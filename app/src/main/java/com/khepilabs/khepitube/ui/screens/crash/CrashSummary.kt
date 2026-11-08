package com.khepilabs.khepitube.ui.screens.crash

import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val NEW_ISSUE_URL = "https://github.com/itz-madan/KhepiTube/issues/new"
private const val APP_PACKAGE = "com.khepilabs.khepitube"
private const val MAX_URL_LOG_CHARS = 1_500
private const val MAX_URL_ACTUAL_CHARS = 300
private const val UNKNOWN_STEPS = "Not known. KhepiTube crashed during normal use; the stack trace is under Logs."
private const val EXPECTED = "KhepiTube keeps running."
private const val TOP_FRAMES = 6
private val ReportTime: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
private val WrappedCause = Regex("""([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+(?:Exception|Error))(?::.*)?""", RegexOption.DOT_MATCHES_ALL)

/** The facts a person needs from a crash report, read back from the text the crash handler saved. */
data class CrashSummary(
    val exception: String,
    val message: String?,
    val location: String?,
    val appVersion: String?,
    val android: String?,
    val device: String?,
    val time: LocalDateTime?,
    val topFrames: List<String>,
) {
    val headline: String
        get() =
            message
                ?.let(WrappedCause::matchEntire)
                ?.groupValues
                ?.get(1)
                ?.substringAfterLast('.')
                ?: exception
}

fun parseCrashSummary(report: String): CrashSummary {
    val lines = report.lines().map { it.trim() }

    fun value(label: String) =
        lines
            .firstOrNull { it.startsWith("$label:") }
            ?.substringAfter(':')
            ?.trim()
            ?.ifBlank { null }
    val frames = lines.filter { it.startsWith("at ") }
    val ownFrame = frames.firstOrNull { APP_PACKAGE in it } ?: frames.firstOrNull()
    val manufacturer = value("Manufacturer")
    val model = value("Model")
    return CrashSummary(
        exception = value("Exception")?.substringAfterLast('.') ?: "Crash",
        message = value("Message")?.takeUnless { it == "none" },
        location = ownFrame?.substringAfterLast('(')?.substringBefore(')'),
        appVersion = value("App Version"),
        android = value("Android"),
        device = listOfNotNull(manufacturer, model).joinToString(" ").ifBlank { null },
        time =
            lines.firstOrNull { it.startsWith("CRASH REPORT - ") }?.substringAfter(" - ")?.let {
                runCatching { LocalDateTime.parse(it, ReportTime) }.getOrNull()
            },
        topFrames = frames.take(TOP_FRAMES),
    )
}

fun CrashSummary.issueUrl(): String {
    val title = "[Bug]: $headline" + (location?.let { " in $it" } ?: "")
    val error = exception + (message?.let { ": $it" } ?: "")
    val log =
        buildString {
            append(error)
            topFrames.forEach { append('\n').append(it) }
        }.take(MAX_URL_LOG_CHARS)
    val fields =
        listOfNotNull(
            "title" to title,
            appVersion?.let { "app-version" to it.substringBefore(' ') },
            android?.let { "android-version" to it },
            device?.let { "device" to it },
            "steps" to UNKNOWN_STEPS,
            "expected" to EXPECTED,
            "actual" to "KhepiTube crashed with $error".take(MAX_URL_ACTUAL_CHARS),
            "logs" to log,
        )
    return NEW_ISSUE_URL + "?" +
        fields.joinToString("&") { (key, value) -> "$key=${URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")}" }
}
