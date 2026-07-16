package com.opfis.data.backup

import app.cash.sqldelight.db.SqlDriver
import com.opfis.domain.backup.BackupPort
import com.opfis.domain.backup.BackupResult
import java.io.File

/**
 * Uses `VACUUM INTO` for a consistent export while the database is
 * open. Restore closes [driver] itself before copying (an open file
 * handle blocks overwriting on Windows, and leaves a live connection
 * pointed at a swapped-out file everywhere else) - the caller must
 * treat a successful restore as requiring an app restart, since every
 * Koin-held `OpfisDatabase`/repository singleton is permanently bound
 * to this now-closed driver instance (ROADMAP Phase 9).
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
            driver.close()
            source.copyTo(File(databaseFilePath), overwrite = true)
        }.fold(
            onSuccess = { BackupResult.Success },
            onFailure = { e -> BackupResult.Failure(e.message ?: "Unknown error during restore") },
        )
}
