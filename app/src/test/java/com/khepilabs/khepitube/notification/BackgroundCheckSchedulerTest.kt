package com.khepilabs.khepitube.notification

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackgroundCheckSchedulerTest {
    @Test
    fun `the subscription check runs only while new video notifications can be posted`() {
        assertThat(BackgroundCheckScheduler.subscriptionCheckWanted(notificationsEnabled = true, newVideosEnabled = true)).isTrue()
        assertThat(BackgroundCheckScheduler.subscriptionCheckWanted(notificationsEnabled = true, newVideosEnabled = false)).isFalse()
        assertThat(BackgroundCheckScheduler.subscriptionCheckWanted(notificationsEnabled = false, newVideosEnabled = true)).isFalse()
    }
}
