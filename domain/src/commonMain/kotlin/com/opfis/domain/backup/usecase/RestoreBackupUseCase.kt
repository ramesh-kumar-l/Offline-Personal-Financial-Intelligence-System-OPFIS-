package com.opfis.domain.backup.usecase

import com.opfis.domain.backup.BackupPort
import com.opfis.domain.backup.BackupResult

/**
 * Restores the whole encrypted database (ROADMAP Phase 9, "Restore").
 * On [BackupResult.Success] the live database driver has been closed
 * (see `FileBackupPort.restoreBackup`) - the caller must treat the app
 * as needing a full restart afterward.
 */
class RestoreBackupUseCase(
    private val backupPort: BackupPort,
) {
    suspend operator fun invoke(sourcePath: String): BackupResult = backupPort.restoreBackup(sourcePath)
}
