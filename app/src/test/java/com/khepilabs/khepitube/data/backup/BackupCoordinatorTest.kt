package com.khepilabs.khepitube.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.google.common.truth.Truth.assertThat
import com.khepilabs.khepitube.data.local.BackupRepository
import com.khepilabs.khepitube.notification.NotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test

class BackupCoordinatorTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: BackupRepository = mockk(relaxed = true)
    private val first: Uri = mockk()
    private val second: Uri = mockk()

    private fun coordinator() =
        BackupCoordinator(
            context,
            repository,
            mockk(relaxed = true),
            mockk(relaxed = true),
        )

    @Before
    fun setUp() {
        mockkStatic(DocumentsContract::class)
        every { DocumentsContract.deleteDocument(any(), any()) } returns true
        mockkObject(NotificationHelper)
        every { NotificationHelper.cancelImportNotification(any()) } returns Unit
        every { NotificationHelper.hasNotificationPermission(any()) } returns false
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `an export started while another runs deletes the file the save dialog created`() {
        val gate = CompletableDeferred<Result<Unit>>()
        coEvery { repository.exportData(first) } coAnswers { gate.await() }
        val coordinator = coordinator()

        assertThat(coordinator.exportAppData(first)).isTrue()
        assertThat(coordinator.exportNewPipeSubscriptions(second)).isFalse()

        verify(timeout = 2_000) { DocumentsContract.deleteDocument(any(), second) }
        verify(exactly = 0) { DocumentsContract.deleteDocument(any(), first) }
        gate.cancel()
    }

    @Test
    fun `a failed export deletes its empty file and reports the failure`() =
        runBlocking {
            coEvery { repository.exportSubscriptionsAsNewPipe(first) } returns Result.failure(IllegalStateException())
            val coordinator = coordinator()

            assertThat(coordinator.exportNewPipeSubscriptions(first)).isTrue()

            val outcome = withTimeout(2_000) { coordinator.operation.first { it !is BackupOperation.Running } }
            assertThat(outcome).isInstanceOf(BackupOperation.Failed::class.java)
            verify { DocumentsContract.deleteDocument(any(), first) }
        }

    @Test
    fun `a successful export keeps its file`() =
        runBlocking {
            coEvery { repository.exportSubscriptionsAsNewPipe(first) } returns
                Result.success(NewPipeSubscriptionExport("{}", skipped = 0))
            val coordinator = coordinator()

            coordinator.exportNewPipeSubscriptions(first)

            val outcome = withTimeout(2_000) { coordinator.operation.first { it !is BackupOperation.Running } }
            assertThat(outcome).isInstanceOf(BackupOperation.Succeeded::class.java)
            verify(exactly = 0) { DocumentsContract.deleteDocument(any(), any()) }
        }

    @Test
    fun `master backup export and import use the current repository API`() =
        runBlocking {
            coEvery { repository.exportMasterBackup(first) } returns Result.success(Unit)
            coEvery { repository.importMasterBackup(second) } returns Result.success(Unit)
            val coordinator = coordinator()

            coordinator.exportMaster(first)
            withTimeout(2_000) { coordinator.operation.first { it is BackupOperation.Succeeded } }
            coordinator.dismiss()
            coordinator.importMaster(second)
            withTimeout(2_000) { coordinator.operation.first { it is BackupOperation.Succeeded } }

            coVerify { repository.exportMasterBackup(first) }
            coVerify { repository.importMasterBackup(second) }
        }

    @Test
    fun `the outcome is published after the last notification call`() =
        runBlocking {
            coEvery { repository.importMasterBackup(second) } returns Result.success(Unit)
            val coordinator = coordinator()
            var sawSuccessDuringNotification = false
            every { NotificationHelper.hasNotificationPermission(any()) } answers {
                if (coordinator.operation.value is BackupOperation.Succeeded) sawSuccessDuringNotification = true
                false
            }

            coordinator.importMaster(second)
            withTimeout(2_000) { coordinator.operation.first { it is BackupOperation.Succeeded } }

            assertThat(sawSuccessDuringNotification).isFalse()
        }
}
