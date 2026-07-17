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
import com.opfis.app.io.readFileBytes
import com.opfis.app.io.rememberAppExitLauncher
import com.opfis.app.io.rememberTempFilePathFactory
import com.opfis.domain.audit.usecase.RecordAuditEventUseCase
import com.opfis.domain.backup.BackupResult
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * Import/export hub (ROADMAP Phase 9): whole-database encrypted
 * backup/restore, a full JSON export/import of every entity, and a CSV
 * export/import scoped to transactions. State/wiring live here;
 * [ImportExportScreenBody] renders the layout. Export/import launcher
 * setup is split into [rememberExportSavers]/[rememberImportPickers] to
 * keep this function under the LongMethod threshold.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun ImportExportScreen() {
    val exports =
        ExportUseCases(
            exportFinancialData = koinInject(),
            exportTransactionsCsv = koinInject(),
            exportBackup = koinInject(),
        )

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    val context =
        ImportExportContext(
            scope = rememberCoroutineScope(),
            recordAuditEvent = koinInject<RecordAuditEventUseCase>(),
            createTempFilePath = rememberTempFilePathFactory(),
            onStatusMessage = { statusMessage = it },
        )
    val savers = rememberExportSavers(context)
    val pickers =
        rememberImportPickers(
            useCases =
                ImportExportUseCases(
                    importFinancialData = koinInject(),
                    importTransactionsCsv = koinInject(),
                    restoreBackup = koinInject(),
                ),
            context = context,
            exitApp = rememberAppExitLauncher(),
        )

    Scaffold(topBar = { TopAppBar(title = { Text("Data") }) }) { padding ->
        ImportExportScreenBody(
            padding = padding,
            statusMessage = statusMessage,
            actions =
                importExportActions(
                    context = context,
                    exports = exports,
                    savers = savers,
                    pickers = pickers,
                    onRestoreBackupClick = { showRestoreConfirm = true },
                ),
        )
    }

    if (showRestoreConfirm) {
        RestoreConfirmDialog(
            onDismiss = { showRestoreConfirm = false },
            onConfirm = {
                showRestoreConfirm = false
                pickers.launchBackupPicker()
            },
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun importExportActions(
    context: ImportExportContext,
    exports: ExportUseCases,
    savers: ExportSavers,
    pickers: ImportPickers,
    onRestoreBackupClick: () -> Unit,
): ImportExportActions =
    ImportExportActions(
        onExportJson = {
            context.scope.launch {
                val json = exports.exportFinancialData(Clock.System.now().toEpochMilliseconds())
                savers.launchJsonSaver("opfis-export.json", json.encodeToByteArray())
            }
        },
        onImportJson = pickers.launchJsonPicker,
        onExportCsv = {
            context.scope.launch {
                savers.launchCsvSaver("opfis-transactions.csv", exports.exportTransactionsCsv().encodeToByteArray())
            }
        },
        onImportCsv = pickers.launchCsvPicker,
        onExportBackup = {
            context.scope.launch {
                val tempPath = context.createTempFilePath("opfis-export-backup", ".db")
                when (val result = exports.exportBackup(tempPath)) {
                    is BackupResult.Success -> savers.launchBackupSaver("opfis-backup.db", readFileBytes(tempPath))
                    is BackupResult.Failure -> context.onStatusMessage("Backup export failed: ${result.reason}")
                }
            }
        },
        onRestoreBackupClick = onRestoreBackupClick,
    )

@Composable
private fun RestoreConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore encrypted backup?") },
        text = {
            Text(
                "This replaces all current data with the backup's contents and closes " +
                    "OPFIS. Reopen the app afterward to see the restored data.",
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Restore") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
