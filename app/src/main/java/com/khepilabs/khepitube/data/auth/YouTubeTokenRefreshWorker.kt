package com.khepilabs.khepitube.data.auth

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.khepilabs.khepitube.innertube.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** Keeps the persisted YouTube OAuth access token fresh while the app is in the background. */
class YouTubeTokenRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            YouTube.accessToken = YouTubeDeviceAuth(applicationContext).accessToken()
            Result.success()
        } catch (_: Exception) {
            if (runAttemptCount < 4) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "youtube_oauth_token_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<YouTubeTokenRefreshWorker>(45, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
                )
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
        }
    }
}
