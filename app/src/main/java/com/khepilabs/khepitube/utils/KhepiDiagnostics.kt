package com.khepilabs.khepitube.utils

import android.content.Context
import android.os.Build

data class DeviceInfo(
    val androidRelease: String = Build.VERSION.RELEASE,
    val sdk: Int = Build.VERSION.SDK_INT,
    val manufacturer: String = Build.MANUFACTURER,
    val model: String = Build.MODEL,
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
)

object KhepiDiagnostics {
    val deviceInfo: DeviceInfo = DeviceInfo()

    fun buildDeviceInfo(context: Context): DeviceInfo = deviceInfo

    fun deviceInfo(context: Context): DeviceInfo = deviceInfo

    fun readSessionLogs(lines: Int = 100): String = ""

    fun crashLogsOrNull(context: Context): String? = null

    fun buildFullReport(
        context: Context,
        sessionText: String = "",
    ): String = "Khepi Diagnostics Report for ${deviceInfo.model}"

    fun clearCrashLogs(context: Context) {}

    fun generateReport(context: Context): String = "Khepi Diagnostics Report for ${deviceInfo.model}"

    fun dumpState(): String = "Khepi Diagnostics State"
}
