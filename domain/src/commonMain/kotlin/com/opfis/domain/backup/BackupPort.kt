package com.opfis.domain.backup

/**
 * Encrypted database backup/restore contract (ROADMAP.md Phase 1,
 * "Backup interfaces"). Phase 9 builds the user-facing import/export
 * flow (CSV/JSON, restore UX) on top of this primitive; this port only
 * guarantees the encrypted file can be copied out and back in safely.
 */
interface BackupPort {
    suspend fun exportBackup(destinationPath: String): BackupResult

    suspend fun restoreBackup(sourcePath: String): BackupResult
}

sealed interface BackupResult {
    data object Success : BackupResult

    data class Failure(
        val reason: String,
    ) : BackupResult
}
