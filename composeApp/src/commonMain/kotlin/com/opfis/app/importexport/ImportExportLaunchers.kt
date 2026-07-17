package com.opfis.app.importexport

import androidx.compose.runtime.Composable
import com.opfis.app.document.rememberDocumentPickerLauncher
import com.opfis.app.io.FileSaveResult
import com.opfis.app.io.rememberFileSaverLauncher
import com.opfis.app.io.writeFileBytes
import com.opfis.domain.audit.AuditEventType
import com.opfis.domain.audit.AuditLogEntry
import com.opfis.domain.audit.usecase.RecordAuditEventUseCase
import com.opfis.domain.backup.BackupResult
import com.opfis.domain.backup.usecase.ExportBackupUseCase
import com.opfis.domain.backup.usecase.RestoreBackupUseCase
import com.opfis.domain.importexport.usecase.ExportFinancialDataUseCase
import com.opfis.domain.importexport.usecase.ExportTransactionsCsvUseCase
import com.opfis.domain.importexport.usecase.ImportFinancialDataUseCase
import com.opfis.domain.importexport.usecase.ImportTransactionsCsvUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Groups [ImportExportScreen]'s cross-cutting collaborators to keep helper-function param lists short. */
internal data class ImportExportContext(
    val scope: CoroutineScope,
    val recordAuditEvent: RecordAuditEventUseCase,
    val createTempFilePath: (prefix: String, suffix: String) -> String,
    val onStatusMessage: (String) -> Unit,
)

internal data class ImportExportUseCases(
    val importFinancialData: ImportFinancialDataUseCase,
    val importTransactionsCsv: ImportTransactionsCsvUseCase,
    val restoreBackup: RestoreBackupUseCase,
)

internal data class ExportUseCases(
    val exportFinancialData: ExportFinancialDataUseCase,
    val exportTransactionsCsv: ExportTransactionsCsvUseCase,
    val exportBackup: ExportBackupUseCase,
)

internal data class ExportSavers(
    val launchJsonSaver: (String, ByteArray) -> Unit,
    val launchCsvSaver: (String, ByteArray) -> Unit,
    val launchBackupSaver: (String, ByteArray) -> Unit,
)

internal data class ImportPickers(
    val launchJsonPicker: () -> Unit,
    val launchCsvPicker: () -> Unit,
    val launchBackupPicker: () -> Unit,
)

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
internal fun auditEntry(
    eventType: AuditEventType,
    description: String,
): AuditLogEntry =
    AuditLogEntry(
        id = Uuid.random().toString(),
        eventType = eventType,
        description = description,
        occurredAt = Clock.System.now().toEpochMilliseconds(),
    )

private fun handleExportResult(
    context: ImportExportContext,
    result: FileSaveResult,
    auditEventType: AuditEventType,
    actionLabel: String,
) {
    when (result) {
        is FileSaveResult.Success -> {
            context.scope.launch { context.recordAuditEvent(auditEntry(auditEventType, "Completed $actionLabel")) }
            context.onStatusMessage("$actionLabel complete.")
        }
        is FileSaveResult.Cancelled -> Unit
        is FileSaveResult.Error -> context.onStatusMessage("$actionLabel failed: ${result.message}")
    }
}

@Composable
internal fun rememberExportSavers(context: ImportExportContext): ExportSavers {
    val launchJsonSaver =
        rememberFileSaverLauncher(mimeType = "application/json") { result ->
            handleExportResult(context, result, AuditEventType.DATA_EXPORTED, "data export (JSON)")
        }
    val launchCsvSaver =
        rememberFileSaverLauncher(mimeType = "text/csv") { result ->
            handleExportResult(context, result, AuditEventType.DATA_EXPORTED, "transaction export (CSV)")
        }
    val launchBackupSaver =
        rememberFileSaverLauncher(mimeType = "application/octet-stream") { result ->
            handleExportResult(context, result, AuditEventType.BACKUP_EXPORTED, "encrypted backup export")
        }
    return ExportSavers(launchJsonSaver, launchCsvSaver, launchBackupSaver)
}

@Composable
internal fun rememberImportPickers(
    useCases: ImportExportUseCases,
    context: ImportExportContext,
    exitApp: () -> Unit,
): ImportPickers {
    val launchJsonPicker =
        rememberDocumentPickerLauncher { picked ->
            context.scope.launch {
                val summary = useCases.importFinancialData(picked.bytes.decodeToString())
                val importDescription = "Imported data from ${picked.fileName}"
                context.recordAuditEvent(auditEntry(AuditEventType.DATA_IMPORTED, importDescription))
                val counts = summary.countsByEntity.entries.joinToString { "${it.key} ${it.value}" }
                context.onStatusMessage("Import complete: $counts")
            }
        }
    val launchCsvPicker =
        rememberDocumentPickerLauncher { picked ->
            context.scope.launch {
                val count = useCases.importTransactionsCsv(picked.bytes.decodeToString())
                val description = "Imported $count transactions from ${picked.fileName}"
                context.recordAuditEvent(auditEntry(AuditEventType.DATA_IMPORTED, description))
                context.onStatusMessage(description)
            }
        }
    val launchBackupPicker =
        rememberDocumentPickerLauncher { picked ->
            context.scope.launch {
                val tempPath = context.createTempFilePath("opfis-restore-backup", ".db")
                val result =
                    runCatching { writeFileBytes(tempPath, picked.bytes) }
                        .mapCatching { useCases.restoreBackup(tempPath) }
                        .getOrElse { BackupResult.Failure(it.message ?: "Unknown error while staging the backup file") }
                when (result) {
                    is BackupResult.Success -> {
                        val description = "Restored encrypted backup from ${picked.fileName}"
                        context.recordAuditEvent(auditEntry(AuditEventType.BACKUP_RESTORED, description))
                        exitApp()
                    }
                    is BackupResult.Failure -> context.onStatusMessage("Restore failed: ${result.reason}")
                }
            }
        }
    return ImportPickers(launchJsonPicker, launchCsvPicker, launchBackupPicker)
}
