package com.khepilabs.khepitube.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.khepilabs.khepitube.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val okHttpClient: OkHttpClient,
    ) {
        private val _downloadProgress = MutableStateFlow(0f)
        val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

        suspend fun checkLatestRelease(): UpdateRelease? =
            withContext(Dispatchers.IO) {
                runCatching {
                    val url =
                        "https://api.github.com/repos/${BuildConfig.GITHUB_RELEASES_OWNER}/${BuildConfig.GITHUB_RELEASES_REPO}/releases/latest"
                    val request =
                        Request
                            .Builder()
                            .url(url)
                            .header("Accept", "application/vnd.github.v3+json")
                            .header("User-Agent", "KhepiTube-Android")
                            .build()

                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@runCatching null
                        val json = JSONObject(response.body?.string().orEmpty())
                        val tagName = json.optString("tag_name", "")
                        val versionName = tagName.removePrefix("v").removePrefix("V")
                        val title = json.optString("name", tagName)
                        val notes = json.optString("body", "")
                        val publishedAt = json.optString("published_at", "")
                        val htmlUrl =
                            json.optString(
                                "html_url",
                                "https://github.com/${BuildConfig.GITHUB_RELEASES_OWNER}/${BuildConfig.GITHUB_RELEASES_REPO}/releases",
                            )

                        var downloadUrl: String? = null
                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name", "")
                                if (name.endsWith(".apk", ignoreCase = true)) {
                                    downloadUrl = asset.optString("browser_download_url")
                                    break
                                }
                            }
                        }

                        val currentVersion = BuildConfig.VERSION_NAME
                        val isNewer = isVersionNewer(versionName, currentVersion)

                        UpdateRelease(
                            tagName = tagName,
                            versionName = versionName,
                            title = title,
                            notes = notes,
                            publishedAt = publishedAt,
                            downloadUrl = downloadUrl,
                            htmlUrl = htmlUrl,
                            isNewer = isNewer,
                        )
                    }
                }.getOrNull()
            }

        suspend fun downloadAndInstallApk(release: UpdateRelease): Boolean =
            withContext(Dispatchers.IO) {
                val url = release.downloadUrl ?: return@withContext false
                runCatching {
                    val request = Request.Builder().url(url).build()
                    val destination = File(context.cacheDir, "khepitube-update.apk")
                    if (destination.exists()) destination.delete()

                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@runCatching false
                        val body = response.body ?: return@runCatching false
                        val contentLength = body.contentLength()

                        body.byteStream().use { input ->
                            destination.outputStream().use { output ->
                                val buffer = ByteArray(8192)
                                var read: Int
                                var downloaded = 0L
                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                    downloaded += read
                                    if (contentLength > 0) {
                                        _downloadProgress.value = downloaded.toFloat() / contentLength.toFloat()
                                    }
                                }
                                output.flush()
                            }
                        }
                    }

                    installApk(context, destination)
                    true
                }.getOrDefault(false)
            }

        private fun installApk(
            context: Context,
            file: File,
        ) {
            val uri: Uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } else {
                    Uri.fromFile(file)
                }

            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            context.startActivity(intent)
        }

        private fun isVersionNewer(
            remote: String,
            local: String,
        ): Boolean {
            val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
            val localParts = local.split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(remoteParts.size, localParts.size)

            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val l = localParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (r < l) return false
            }
            return false
        }
    }
