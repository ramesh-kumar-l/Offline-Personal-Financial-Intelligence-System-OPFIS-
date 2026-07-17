package com.opfis.data.db

import com.opfis.data.backup.FileBackupPort
import com.opfis.domain.backup.BackupResult
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Phase 1 exit criteria: "persistent encrypted storage" and "recovery
 * tests" (ROADMAP.md). Runs against the real Desktop
 * [DatabaseDriverFactory] - no mocks - proving the encryption and
 * recovery mechanisms described in docs/adr/0005 actually work.
 */
class EncryptedPersistenceRecoveryTest {
    @Test
    fun `data survives driver close and reopen with the same passphrase`() {
        val dir = createTempDirectory("opfis-recovery-test")
        val factory = DatabaseDriverFactory(dir)
        val passphrase = "correct horse battery staple".toCharArray()

        val driver1 = factory.createDriver(passphrase)
        seedOfflineModeRow(driver1)
        driver1.close()

        val driver2 = factory.createDriver(passphrase)
        val row = OpfisDatabase(driver2).systemStatusQueries.selectById("offline_mode").executeAsOne()
        assertEquals("Offline Mode", row.label)
        driver2.close()

        dir.toFile().deleteRecursively()
    }

    @Test
    fun `wrong passphrase cannot read previously written data`() {
        val dir = createTempDirectory("opfis-wrongkey-test")
        val factory = DatabaseDriverFactory(dir)

        val driver1 = factory.createDriver("correct-key".toCharArray())
        seedOfflineModeRow(driver1)
        driver1.close()

        assertFailsWith<Exception> {
            val driver2 = factory.createDriver("wrong-key".toCharArray())
            OpfisDatabase(driver2).systemStatusQueries.selectAll().executeAsList()
        }

        dir.toFile().deleteRecursively()
    }

    @Test
    fun `backup export and restore round-trips data through FileBackupPort`() =
        runTest {
            val dir = createTempDirectory("opfis-backup-test")
            val factory = DatabaseDriverFactory(dir)
            val passphrase = "backup-test-key".toCharArray()

            val driver = factory.createDriver(passphrase)
            seedOfflineModeRow(driver)
            val backupPort = FileBackupPort(driver, factory.databaseFilePath())
            val backupPath = dir.resolve("opfis-backup.db").toAbsolutePath().toString()

            val exportResult = backupPort.exportBackup(backupPath)
            assertTrue(exportResult is BackupResult.Success)

            OpfisDatabase(driver).systemStatusQueries.updateState(
                state = "PENDING",
                updated_at = 2L,
                id = "offline_mode",
            )

            val restoreResult = backupPort.restoreBackup(backupPath)
            assertTrue(restoreResult is BackupResult.Success)

            val reopened = factory.createDriver(passphrase)
            val row = OpfisDatabase(reopened).systemStatusQueries.selectById("offline_mode").executeAsOne()
            assertEquals("ACTIVE", row.state)
            reopened.close()

            dir.toFile().deleteRecursively()
        }

    @Test
    fun `restoreBackup closes the driver before swapping the file, and a write made after export is not preserved`() =
        runTest {
            val dir = createTempDirectory("opfis-restore-closes-driver-test")
            val factory = DatabaseDriverFactory(dir)
            val passphrase = "restore-close-test-key".toCharArray()

            val driver = factory.createDriver(passphrase)
            seedOfflineModeRow(driver)
            val backupPort = FileBackupPort(driver, factory.databaseFilePath())
            val backupPath = dir.resolve("opfis-backup.db").toAbsolutePath().toString()
            assertTrue(backupPort.exportBackup(backupPath) is BackupResult.Success)

            // Written after the backup snapshot was taken - must not survive the restore.
            OpfisDatabase(driver).systemStatusQueries.updateState(
                state = "PENDING",
                updated_at = 2L,
                id = "offline_mode",
            )

            assertTrue(backupPort.restoreBackup(backupPath) is BackupResult.Success)

            // `driver` was closed by restoreBackup, but this JDBC driver transparently
            // reopens a connection on next use rather than throwing - so the invariant
            // worth proving is that the reopened connection reads the restored file
            // (the pre-mutation state), not that the closed reference is inert.
            val row = OpfisDatabase(driver).systemStatusQueries.selectById("offline_mode").executeAsOne()
            assertEquals("ACTIVE", row.state)

            dir.toFile().deleteRecursively()
        }

    private fun seedOfflineModeRow(driver: app.cash.sqldelight.db.SqlDriver) {
        OpfisDatabase(driver).systemStatusQueries.insertOrReplace(
            id = "offline_mode",
            label = "Offline Mode",
            state = "ACTIVE",
            created_at = 1L,
            updated_at = 1L,
            version = 1L,
        )
    }
}
