package com.khepilabs.khepitube.ui.screens.settings

import com.khepilabs.khepitube.ui.screens.settings.topics.unblockedSuggestions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.util.Locale

class BackupAndTopicsHelpersTest {
    @Test
    fun `blocked suggestions hide whatever the case they were stored in`() {
        val remaining = unblockedSuggestions(listOf("ASMR", "News", "drama"), blocked = setOf("asmr", "drama"), locale = Locale.ROOT)
        assertEquals(listOf("News"), remaining)
    }
}
