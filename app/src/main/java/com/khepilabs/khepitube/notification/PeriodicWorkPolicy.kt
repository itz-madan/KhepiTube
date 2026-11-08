package com.khepilabs.khepitube.notification

import androidx.work.ExistingPeriodicWorkPolicy

internal fun periodicWorkPolicy(reschedule: Boolean): ExistingPeriodicWorkPolicy =
    if (reschedule) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP
