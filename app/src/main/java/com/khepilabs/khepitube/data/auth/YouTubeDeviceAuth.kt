package com.khepilabs.khepitube.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.concurrent.TimeUnit

/**
 * YouTube's device-code OAuth flow used by MeeTube.
 *
 * Credentials are deliberately discovered from YouTube's current TV bootstrap/player script;
 * no OAuth client secret is embedded in the application.
 */
class YouTubeDeviceAuth(context: Context) {
    data class DeviceCode(
        val deviceCode: String,
        val userCode: String,
        val verificationUrl: String,
        val expiresInSeconds: Long,
        val pollIntervalSeconds: Long,
    )

    data class SignedInAccount(
        val accessToken: String,
        val refreshToken: String,
        val expiresInSeconds: Long,
        val name: String?,
        val avatarUrl: String?,
    )

    private data class OAuthClient(val id: String, val secret: String)

    private val appContext = context.applicationContext
    private val store = SecureTokenStore(appContext)
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    /** Starts a fresh device authorization. Never fabricates a user/device code. */
    fun requestDeviceCode(): DeviceCode {
        val client = discoverOAuthClient()
        val deviceId = store.deviceId()
        val payload = JSONObject()
            .put("client_id", client.id)
            .put("device_id", deviceId)
            .put("model_name", "ytlr::")
            .put("scope", "http://gdata.youtube.com https://www.googleapis.com/auth/youtube-paid-content")
        val response = postJson("https://www.youtube.com/o/oauth2/device/code", payload)
        return DeviceCode(
            deviceCode = response.getString("device_code"),
            userCode = response.getString("user_code"),
            verificationUrl = response.optString("verification_url").ifBlank { "https://youtube.com/activate" },
            expiresInSeconds = response.optLong("expires_in", 1800L),
            pollIntervalSeconds = response.optLong("interval", 5L).coerceAtLeast(1L),
        ).also { store.savePendingClient(client.id, client.secret) }
    }

    /**
     * Poll once. Returns null while the user has not approved yet; throws for expired,
     * denied, network, and other OAuth errors. The caller schedules the next poll using interval.
     */
    fun pollApproval(deviceCode: String): SignedInAccount? {
        val credentials = store.pendingClient() ?: error("No pending device authorization")
        val payload = JSONObject()
            .put("code", deviceCode)
            .put("client_id", credentials.first)
            .put("client_secret", credentials.second)
            .put("grant_type", "http://oauth.net/grant_type/device/1.0")
        val response = postJsonAllowOAuthErrors("https://www.youtube.com/o/oauth2/token", payload)
        when (response.optString("error")) {
            "authorization_pending", "slow_down" -> return null
            "expired_token" -> error("Activation code expired. Please try again.")
            "access_denied" -> error("Google sign-in was denied.")
            "" -> Unit
            else -> error(response.optString("error_description", response.optString("error")))
        }
        val refresh = response.optString("refresh_token").takeIf { it.isNotBlank() }
            ?: error("Google did not return a refresh token")
        val access = response.optString("access_token").takeIf { it.isNotBlank() }
            ?: error("Google did not return an access token")
        val expires = response.optLong("expires_in", 3600L)
        store.saveRefreshToken(refresh)
        store.saveAccessToken(access, System.currentTimeMillis() + expires * 1000L)
        val profile = runCatching { fetchAccountProfile(access) }.getOrDefault(null to null)
        store.saveProfile(profile.first, profile.second)
        store.clearPendingClient()
        return SignedInAccount(access, refresh, expires, profile.first, profile.second)
    }


    private fun fetchAccountProfile(accessToken: String): Pair<String?, String?> {
        val body = JSONObject()
            .put("context", JSONObject().put("client", JSONObject()
                .put("clientName", "TVHTML5").put("clientVersion", "7.20260128")))
            .put("accountReadMask", JSONObject()
                .put("returnOwner", true).put("returnBrandAccounts", true).put("returnPersonaAccounts", false))
        val request = Request.Builder().url("https://www.youtube.com/youtubei/v1/account/accounts_list")
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .header("X-YouTube-Client-Name", "TVHTML5")
            .header("X-YouTube-Client-Version", "7.20260128")
            .header("Origin", "https://www.youtube.com")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; TV)")
            .build()
        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful || raw.isBlank()) error("Could not load YouTube account profile (HTTP ${response.code})")
            val account = findAccountObject(JSONObject(raw)) ?: error("YouTube did not return an account profile")
            val nameValue = account.opt("accountName")
            val name = if (nameValue is JSONObject) nameValue.optString("simpleText") else nameValue?.toString()
            val thumbnails = account.optJSONObject("accountPhoto")?.optJSONArray("thumbnails")
            val avatar = thumbnails?.let { items ->
                (0 until items.length()).mapNotNull { items.optJSONObject(it)?.optString("url") }
                    .lastOrNull { it.isNotBlank() }
            }
            return name?.takeIf { it.isNotBlank() } to avatar
        }
    }

    private fun findAccountObject(value: Any?): JSONObject? {
        when (value) {
            is JSONObject -> {
                if (value.has("accountName") && value.has("accountPhoto")) return value
                val keys = value.keys()
                while (keys.hasNext()) findAccountObject(value.opt(keys.next()))?.let { return it }
            }
            is org.json.JSONArray -> for (i in 0 until value.length()) findAccountObject(value.opt(i))?.let { return it }
        }
        return null
    }

    /** Returns a valid access token, refreshing it before expiry. */
    fun accessToken(): String {
        val refresh = store.refreshToken() ?: error("Not signed in")
        val expiry = store.accessTokenExpiry()
        val cached = store.accessToken()
        if (!cached.isNullOrBlank() && System.currentTimeMillis() < expiry - 60_000L) return cached

        val client = discoverOAuthClient()
        val payload = JSONObject()
            .put("refresh_token", refresh)
            .put("client_id", client.id)
            .put("client_secret", client.secret)
            .put("grant_type", "refresh_token")
        val response = postJson("https://www.youtube.com/o/oauth2/token", payload)
        val token = response.getString("access_token")
        store.saveAccessToken(token, System.currentTimeMillis() + response.optLong("expires_in", 3600L) * 1000L)
        return token
    }

    fun savedProfile(): Pair<String?, String?> = store.profile()
    fun hasRefreshToken(): Boolean = !store.refreshToken().isNullOrBlank()
    fun signOut() = store.clearAll()

    private fun discoverOAuthClient(): OAuthClient {
        val tv = get("https://www.youtube.com/tv")
        val clientPath = Regex("""id=["']base-js["']\s+src=["']([^"']+)["']""")
            .find(tv)?.groupValues?.get(1)
            ?: Regex("""\.src\s*=\s*'([^']*m=base)'""").find(tv)?.groupValues?.get(1)
            ?: error("Could not discover YouTube TV client script")
        val scriptUrl = if (clientPath.startsWith("http")) clientPath else "https://www.youtube.com$clientPath"
        val script = get(scriptUrl)
        val match = Regex("""clientId:"([-\w]+\.apps\.googleusercontent\.com)",\s*\w+:"(\w+)""")
            .find(script) ?: error("YouTube OAuth client credentials were not found in the current TV client")
        return OAuthClient(match.groupValues[1], match.groupValues[2])
    }

    private fun get(url: String): String {
        val request = Request.Builder().url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; TV) AppleWebKit/537.36 Chrome/120 Safari/537.36")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("YouTube bootstrap failed: HTTP ${response.code}")
            return response.body?.string() ?: error("Empty YouTube bootstrap response")
        }
    }

    private fun postJson(url: String, body: JSONObject): JSONObject {
        val result = postJsonAllowOAuthErrors(url, body)
        if (result.has("error")) error(result.optString("error_description", result.optString("error")))
        return result
    }

    private fun postJsonAllowOAuthErrors(url: String, body: JSONObject): JSONObject {
        val request = Request.Builder().url(url)
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .header("Content-Type", "application/json")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; TV)")
            .build()
        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (raw.isBlank()) error("Empty OAuth response (HTTP ${response.code})")
            return JSONObject(raw)
        }
    }

    private class SecureTokenStore(context: Context) {
        private val prefs = context.getSharedPreferences("youtube_device_auth", Context.MODE_PRIVATE)
        private val alias = "khepitube_youtube_oauth_v1"

        private fun key(): SecretKey {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            (ks.getKey(alias, null) as? SecretKey)?.let { return it }
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            generator.init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            return generator.generateKey()
        }

        private fun putSecret(name: String, value: String?) {
            if (value == null) {
                prefs.edit().remove(name).apply()
                return
            }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val packed = cipher.iv + encrypted
            prefs.edit().putString(name, Base64.encodeToString(packed, Base64.NO_WRAP)).apply()
        }

        private fun getSecret(name: String): String? = runCatching {
            val packed = Base64.decode(prefs.getString(name, null), Base64.NO_WRAP)
            val iv = packed.copyOfRange(0, 12)
            val encrypted = packed.copyOfRange(12, packed.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }.getOrNull()

        fun deviceId(): String = prefs.getString("device_id", null) ?: java.util.UUID.randomUUID().toString()
            .also { prefs.edit().putString("device_id", it).apply() }
        fun savePendingClient(id: String, secret: String) { putSecret("pending_client_id", id); putSecret("pending_client_secret", secret) }
        fun pendingClient(): Pair<String, String>? {
            val id = getSecret("pending_client_id") ?: return null
            val secret = getSecret("pending_client_secret") ?: return null
            return id to secret
        }
        fun clearPendingClient() { putSecret("pending_client_id", null); putSecret("pending_client_secret", null) }
        fun saveRefreshToken(value: String) = putSecret("refresh_token", value)
        fun refreshToken(): String? = getSecret("refresh_token")
        fun saveAccessToken(value: String, expiry: Long) { putSecret("access_token", value); prefs.edit().putLong("access_expiry", expiry).apply() }
        fun accessToken(): String? = getSecret("access_token")
        fun accessTokenExpiry(): Long = prefs.getLong("access_expiry", 0L)
        fun saveProfile(name: String?, avatar: String?) {
            putSecret("profile_name", name)
            putSecret("profile_avatar", avatar)
        }
        fun profile(): Pair<String?, String?> = getSecret("profile_name") to getSecret("profile_avatar")
        fun clearAll() { prefs.edit().clear().apply() }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
