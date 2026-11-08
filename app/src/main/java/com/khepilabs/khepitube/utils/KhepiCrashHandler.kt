package com.khepilabs.khepitube.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ConcurrentLinkedDeque

private const val CRASH_PREFS_NAME = "khepitube_crash_prefs"
private const val CRASH_PREFS_KEY_LAST = "last_crash"

class KhepiCrashHandler private constructor(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
    companion object {
        private const val TAG = "KhepiCrashHandler"
        private const val CRASH_LOG_FILE = "khepitube_crashes.log"
        private const val MAX_CRASH_LOG_SIZE = 500_000L
        private const val MAX_BREADCRUMBS = 40
        private val breadcrumbs = ConcurrentLinkedDeque<KhepiCrashBreadcrumb>()

        @Volatile
        private var instance: KhepiCrashHandler? = null

        fun install(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
                        instance = KhepiCrashHandler(context.applicationContext, defaultHandler)
                        Thread.setDefaultUncaughtExceptionHandler(instance)
                        Log.i(TAG, "Crash handler installed")
                    }
                }
            }
        }

        fun getLastCrash(context: Context): String? =
            context
                .getSharedPreferences(CRASH_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(CRASH_PREFS_KEY_LAST, null)

        fun clearLastCrash(context: Context) {
            context
                .getSharedPreferences(CRASH_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(CRASH_PREFS_KEY_LAST)
                .apply()
        }

        fun getCrashLogs(context: Context): String =
            try {
                val file = File(context.filesDir, CRASH_LOG_FILE)
                if (file.exists()) file.readText() else "No crash logs"
            } catch (e: Exception) {
                "Error reading crash logs: ${e.message}"
            }

        fun clearCrashLogs(context: Context) {
            try {
                File(context.filesDir, CRASH_LOG_FILE).delete()
                Log.i(TAG, "Crash logs cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing crash logs", e)
            }
        }

        fun recordPhase(
            phase: String,
            detail: String,
        ) {
            breadcrumbs.addLast(
                KhepiCrashBreadcrumb(
                    timestampMs = System.currentTimeMillis(),
                    phase = phase.take(48),
                    detail = detail.take(240),
                ),
            )
            while (breadcrumbs.size > MAX_BREADCRUMBS) {
                breadcrumbs.pollFirst()
            }
        }
    }

    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable,
    ) {
        try {
            Log.e(TAG, "=== UNCAUGHT EXCEPTION ===")
            Log.e(TAG, "Thread: ${thread.name} (id=${thread.id})")
            Log.e(TAG, "Exception: ${throwable.javaClass.simpleName}: ${throwable.message}")
            Log.e(TAG, getStackTraceString(throwable))

            val stackTrace = getStackTraceString(throwable)
            val summary = buildCrashReport(thread, throwable, stackTrace)
            context
                .getSharedPreferences(CRASH_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(CRASH_PREFS_KEY_LAST, summary)
                .commit()

            saveCrashToFile(thread, throwable)
        } catch (e: Exception) {
            Log.e(TAG, "Error in crash handler", e)
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrashToFile(
        thread: Thread,
        throwable: Throwable,
    ) {
        try {
            val file = File(context.filesDir, CRASH_LOG_FILE)

            if (file.exists() && file.length() > MAX_CRASH_LOG_SIZE) {
                val backup = File(context.filesDir, "${CRASH_LOG_FILE}.old")
                file.renameTo(backup)
            }

            val stackTrace = getStackTraceString(throwable)
            val formattedCrash = buildCrashReport(thread, throwable, stackTrace)

            val crashReport =
                buildString {
                    appendLine(formattedCrash)
                    appendLine()
                }

            file.appendText(crashReport)
            Log.i(TAG, "Crash saved to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash to file", e)
        }
    }

    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    private fun buildCrashReport(
        thread: Thread,
        throwable: Throwable,
        stackTrace: String,
    ): String {
        val snapshot =
            KhepiCrashReportSnapshot(
                timestampMs = System.currentTimeMillis(),
                threadName = thread.name,
                threadId = thread.id,
                exceptionClass = throwable.javaClass.name,
                exceptionMessage = throwable.message,
                stackTrace = stackTrace,
                deviceInfo = buildDeviceInfo(),
                memoryInfo = buildMemoryInfo(),
                breadcrumbs = breadcrumbs.toList(),
            )
        return KhepiCrashReportFormatter.build(snapshot)
    }

    private fun buildDeviceInfo(): String =
        buildString {
            appendLine("  Model: ${Build.MODEL}")
            appendLine("  Manufacturer: ${Build.MANUFACTURER}")
            appendLine("  Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("  Brand: ${Build.BRAND}")
            appendLine("  Device: ${Build.DEVICE}")
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
                appendLine("  App Version: ${packageInfo.versionName} ($versionCode)")
            } catch (e: Exception) {
                appendLine("  App Version: Unknown")
            }
        }

    private fun buildMemoryInfo(): String {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        return buildString {
            appendLine("  Used heap: ${used / 1024 / 1024} MB")
            appendLine("  Free heap: ${runtime.freeMemory() / 1024 / 1024} MB")
            appendLine("  Total heap: ${runtime.totalMemory() / 1024 / 1024} MB")
            appendLine("  Max heap: ${runtime.maxMemory() / 1024 / 1024} MB")
        }.trimEnd()
    }
}
