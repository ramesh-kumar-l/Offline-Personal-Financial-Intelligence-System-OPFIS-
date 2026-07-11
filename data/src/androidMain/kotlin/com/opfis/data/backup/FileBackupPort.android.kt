package com.opfis.data.backup

import app.cash.sqldelight.db.SqlDriver
import com.opfis.domain.backup.BackupPort
import com.opfis.domain.backup.BackupResult
import java.io.File

/**
 * Uses `VACUUM INTO` for a consistent export while the database is
 * open, and a plain file copy for restore (caller must close the live
 * [SqlDriver] first - see docs/adr/0005 and the recovery tests in
 * `:data`'s desktopTest, which exercise this same mechanism).
 */
class FileBackupPort(
    private val driver: SqlDriver,
    private val databaseFilePath: String,
) : BackupPort {
    override suspend fun exportBackup(destinationPath: String): BackupResult =
        runCatching {
            driver.execute(null, "VACUUM INTO ?", 1) {
                bindString(0, destinationPath)
            }
        }.fold(
            onSuccess = { BackupResult.Success },
            onFailure = { e -> BackupResult.Failure(e.message ?: "Unknown error during export") },
        )

    override suspend fun restoreBackup(sourcePath: String): BackupResult =
        runCatching {
            val source = File(sourcePath)
            require(source.exists()) { "Backup file not found: $sourcePath" }
            source.copyTo(File(databaseFilePath), overwrite = true)
        }.fold(
            onSuccess = { BackupResult.Success },
            onFailure = { e -> BackupResult.Failure(e.message ?: "Unknown error during restore") },
        )
}
