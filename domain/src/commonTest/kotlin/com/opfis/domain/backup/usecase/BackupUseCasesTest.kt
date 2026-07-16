package com.opfis.domain.backup.usecase

import com.opfis.domain.backup.BackupPort
import com.opfis.domain.backup.BackupResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeBackupPort(
    private val exportResult: BackupResult = BackupResult.Success,
    private val restoreResult: BackupResult = BackupResult.Success,
) : BackupPort {
    var lastExportDestination: String? = null
    var lastRestoreSource: String? = null

    override suspend fun exportBackup(destinationPath: String): BackupResult {
        lastExportDestination = destinationPath
        return exportResult
    }

    override suspend fun restoreBackup(sourcePath: String): BackupResult {
        lastRestoreSource = sourcePath
        return restoreResult
    }
}

class BackupUseCasesTest {
    @Test
    fun `export backup delegates to the port with the given destination`() =
        runTest {
            val port = FakeBackupPort()

            val result = ExportBackupUseCase(port)("/tmp/backup.db")

            assertEquals(BackupResult.Success, result)
            assertEquals("/tmp/backup.db", port.lastExportDestination)
        }

    @Test
    fun `export backup surfaces a failure from the port`() =
        runTest {
            val port = FakeBackupPort(exportResult = BackupResult.Failure("disk full"))

            val result = ExportBackupUseCase(port)("/tmp/backup.db")

            assertEquals(BackupResult.Failure("disk full"), result)
        }

    @Test
    fun `restore backup delegates to the port with the given source`() =
        runTest {
            val port = FakeBackupPort()

            val result = RestoreBackupUseCase(port)("/tmp/backup.db")

            assertEquals(BackupResult.Success, result)
            assertEquals("/tmp/backup.db", port.lastRestoreSource)
        }

    @Test
    fun `restore backup surfaces a failure from the port`() =
        runTest {
            val port = FakeBackupPort(restoreResult = BackupResult.Failure("file not found"))

            val result = RestoreBackupUseCase(port)("/tmp/backup.db")

            assertEquals(BackupResult.Failure("file not found"), result)
        }
}
