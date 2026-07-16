package com.opfis.app.importexport

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.opfis.app.document.rememberDocumentPickerLauncher
import com.opfis.app.io.FileSaveResult
import com.opfis.app.io.readFileBytes
import com.opfis.app.io.rememberAppExitLauncher
import com.opfis.app.io.rememberFileSaverLauncher
import com.opfis.app.io.rememberTempFilePathFactory
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Import/export hub (ROADMAP Phase 9): whole-database encrypted
 * backup/restore, a full JSON export/import of every entity, and a CSV
 * export/import scoped to transactions. State/wiring live here;
 * [ImportExportScreenBody] renders the layout.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun ImportExportScreen() {
    val exportFinancialData = koinInject<ExportFinancialDataUseCase>()
    val importFinancialData = koinInject<ImportFinancialDataUseCase>()
    val exportTransactionsCsv = koinInject<ExportTransactionsCsvUseCase>()
    val importTransactionsCsv = koinInject<ImportTransactionsCsvUseCase>()
    val exportBackup = koinInject<ExportBackupUseCase>()
    val restoreBackup = koinInject<RestoreBackupUseCase>()
    val recordAuditEvent = koinInject<RecordAuditEventUseCase>()
    val scope = rememberCoroutineScope()
    val createTempFilePath = rememberTempFilePathFactory()
    val exitApp = rememberAppExitLauncher()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    fun handleExportResult(
        result: FileSaveResult,
        auditEventType: AuditEventType,
        actionLabel: String,
    ) {
        when (result) {
            is FileSaveResult.Success -> {
                scope.launch { recordAuditEvent(auditEntry(auditEventType, "Completed $actionLabel")) }
                statusMessage = "$actionLabel complete."
            }
            is FileSaveResult.Cancelled -> Unit
            is FileSaveResult.Error -> statusMessage = "$actionLabel failed: ${result.message}"
        }
    }

    val launchJsonSaver =
        rememberFileSaverLauncher(mimeType = "application/json") { result ->
            handleExportResult(result, AuditEventType.DATA_EXPORTED, "data export (JSON)")
        }
    val launchCsvSaver =
        rememberFileSaverLauncher(mimeType = "text/csv") { result ->
            handleExportResult(result, AuditEventType.DATA_EXPORTED, "transaction export (CSV)")
        }
    val launchBackupSaver =
        rememberFileSaverLauncher(mimeType = "application/octet-stream") { result ->
            handleExportResult(result, AuditEventType.BACKUP_EXPORTED, "encrypted backup export")
        }

    val launchJsonPicker =
        rememberDocumentPickerLauncher { picked ->
            scope.launch {
                val summary = importFinancialData(picked.bytes.decodeToString())
                recordAuditEvent(auditEntry(AuditEventType.DATA_IMPORTED, "Imported data from ${picked.fileName}"))
                statusMessage = "Import complete: " + summary.countsByEntity.entries.joinToString { "${it.key} ${it.value}" }
            }
        }
    val launchCsvPicker =
        rememberDocumentPickerLauncher { picked ->
            scope.launch {
                val count = importTransactionsCsv(picked.bytes.decodeToString())
                recordAuditEvent(auditEntry(AuditEventType.DATA_IMPORTED, "Imported $count transactions from ${picked.fileName}"))
                statusMessage = "Imported $count transactions from ${picked.fileName}"
            }
        }
    val launchBackupPicker =
        rememberDocumentPickerLauncher { picked ->
            scope.launch {
                val tempPath = createTempFilePath("opfis-restore-backup", ".db")
                val result =
                    runCatching { writeFileBytes(tempPath, picked.bytes) }
                        .mapCatching { restoreBackup(tempPath) }
                        .getOrElse { BackupResult.Failure(it.message ?: "Unknown error while staging the backup file") }
                when (result) {
                    is BackupResult.Success -> {
                        recordAuditEvent(auditEntry(AuditEventType.BACKUP_RESTORED, "Restored encrypted backup from ${picked.fileName}"))
                        exitApp()
                    }
                    is BackupResult.Failure -> statusMessage = "Restore failed: ${result.reason}"
                }
            }
        }

    Scaffold(topBar = { TopAppBar(title = { Text("Data") }) }) { padding ->
        ImportExportScreenBody(
            padding = padding,
            statusMessage = statusMessage,
            actions =
                ImportExportActions(
                    onExportJson = {
                        scope.launch {
                            val json = exportFinancialData(Clock.System.now().toEpochMilliseconds())
                            launchJsonSaver("opfis-export.json", json.encodeToByteArray())
                        }
                    },
                    onImportJson = launchJsonPicker,
                    onExportCsv = {
                        scope.launch { launchCsvSaver("opfis-transactions.csv", exportTransactionsCsv().encodeToByteArray()) }
                    },
                    onImportCsv = launchCsvPicker,
                    onExportBackup = {
                        scope.launch {
                            val tempPath = createTempFilePath("opfis-export-backup", ".db")
                            when (val result = exportBackup(tempPath)) {
                                is BackupResult.Success -> launchBackupSaver("opfis-backup.db", readFileBytes(tempPath))
                                is BackupResult.Failure -> statusMessage = "Backup export failed: ${result.reason}"
                            }
                        }
                    },
                    onRestoreBackupClick = { showRestoreConfirm = true },
                ),
        )
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Restore encrypted backup?") },
            text = {
                Text(
                    "This replaces all current data with the backup's contents and closes " +
                        "OPFIS. Reopen the app afterward to see the restored data.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirm = false
                    launchBackupPicker()
                }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { showRestoreConfirm = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
private fun auditEntry(
    eventType: AuditEventType,
    description: String,
): AuditLogEntry =
    AuditLogEntry(
        id = Uuid.random().toString(),
        eventType = eventType,
        description = description,
        occurredAt = Clock.System.now().toEpochMilliseconds(),
    )
