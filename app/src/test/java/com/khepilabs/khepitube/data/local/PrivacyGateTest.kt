package com.khepilabs.khepitube.data.local

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PrivacyGateTest {
    private fun gate(
        deepFlow: Boolean,
        historyPaused: Boolean = false,
    ) = PrivacyGate(
        deepFlowActive = { deepFlow },
        watchHistoryPaused = { historyPaused },
    )

    @Test
    fun `the gate reports both settings as they are`() =
        runTest {
            val gate = gate(deepFlow = true, historyPaused = true)

            assertThat(gate.isDeepFlowActive()).isTrue()
            assertThat(gate.isWatchHistoryPaused()).isTrue()
        }
}
