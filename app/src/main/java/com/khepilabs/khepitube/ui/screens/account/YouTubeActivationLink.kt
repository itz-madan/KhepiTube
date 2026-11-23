package com.khepilabs.khepitube.ui.screens.account

/**
 * Builds the direct YouTube device-activation URL used by the code-based sign-in flow.
 *
 * YouTube expects spaces in the displayed user code to be represented as hyphens in this URL.
 * The code itself must come from the sign-in service; this helper deliberately does not invent
 * activation codes.
 */
internal object YouTubeActivationLink {
    private const val ACTIVATION_URL = "https://youtube.com/qr/activate/"

    fun forUserCode(userCode: String): String {
        val pathCode = userCode.trim().replace(Regex("\\s+"), "-")
        require(pathCode.isNotEmpty()) { "Activation code must not be blank" }
        return ACTIVATION_URL + pathCode
    }
}
