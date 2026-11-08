package com.khepilabs.khepitube.utils

data class KhepiCrashBreadcrumb(
    val timestampMs: Long,
    val category: String = "",
    val message: String = "",
    val phase: String = category,
    val detail: String = message,
)

data class KhepiCrashReportSnapshot(
    val timestampMs: Long,
    val threadName: String,
    val threadId: Long,
    val exceptionClass: String,
    val exceptionMessage: String?,
    val stackTrace: String,
    val deviceInfo: String,
    val memoryInfo: String,
    val breadcrumbs: List<KhepiCrashBreadcrumb>,
)

object KhepiCrashReportFormatter {
    fun build(snapshot: KhepiCrashReportSnapshot): String =
        buildString {
            appendLine("--- Crash Input ---")
            appendLine("Time: ${snapshot.timestampMs}")
            appendLine("Exception: ${snapshot.exceptionClass}")
            appendLine("Message: ${snapshot.exceptionMessage.orEmpty()}")
            appendLine("OutOfMemoryError: ${snapshot.exceptionClass.contains("OutOfMemoryError")}")
            appendLine()
            appendLine("--- Device Info ---")
            appendLine(snapshot.deviceInfo)
            appendLine(snapshot.memoryInfo)
            appendLine()
            appendLine("--- State/Cache Updates Before Crash ---")
            snapshot.breadcrumbs.forEach { breadcrumb ->
                appendLine("[${breadcrumb.timestampMs}] ${breadcrumb.category}: ${breadcrumb.message}")
            }
            appendLine()
            appendLine("--- Final Output ---")
            appendLine("Top Stack Frames (same build):")
            topStackFrames(snapshot.stackTrace, limit = 5).forEach { frame ->
                appendLine(frame)
            }
            appendLine()
            appendLine("Full Stack Trace:")
            appendLine(snapshot.stackTrace)
        }

    fun topStackFrames(
        stackTrace: String,
        limit: Int,
    ): List<String> =
        stackTrace
            .lines()
            .map { it.trim() }
            .filter { it.startsWith("at ") || it.startsWith("Caused by:") }
            .take(limit)
}
