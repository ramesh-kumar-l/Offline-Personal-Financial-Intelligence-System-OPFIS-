package com.opfis.domain.backup.usecase

import com.opfis.domain.backup.BackupPort
import com.opfis.domain.backup.BackupResult

/** Exports the whole encrypted database (ROADMAP Phase 9, "Encrypted backup"). */
class ExportBackupUseCase(
    private val backupPort: BackupPort,
) {
    suspend operator fun invoke(destinationPath: String): BackupResult = backupPort.exportBackup(destinationPath)
}
