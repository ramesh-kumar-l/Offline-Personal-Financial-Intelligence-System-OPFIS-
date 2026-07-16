package com.opfis.app.importexport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Groups [ImportExportScreenBody]'s callbacks to keep the composable's parameter count in check. */
internal data class ImportExportActions(
    val onExportJson: () -> Unit,
    val onImportJson: () -> Unit,
    val onExportCsv: () -> Unit,
    val onImportCsv: () -> Unit,
    val onExportBackup: () -> Unit,
    val onRestoreBackupClick: () -> Unit,
)

@Composable
internal fun ImportExportScreenBody(
    padding: PaddingValues,
    statusMessage: String?,
    actions: ImportExportActions,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (statusMessage != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(statusMessage, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        ImportExportSection(
            title = "All data (JSON)",
            description = "Every account, transaction, and other record in one portable file.",
            onExport = actions.onExportJson,
            onImport = actions.onImportJson,
        )

        ImportExportSection(
            title = "Transactions (CSV)",
            description = "A spreadsheet-friendly export of your transactions only.",
            onExport = actions.onExportCsv,
            onImport = actions.onImportCsv,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Encrypted backup", style = MaterialTheme.typography.titleMedium)
            Text(
                "A full, encrypted copy of your database - the most complete way to move OPFIS to another device.",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = actions.onExportBackup, modifier = Modifier.fillMaxWidth()) {
                Text("Export encrypted backup")
            }
            OutlinedButton(onClick = actions.onRestoreBackupClick, modifier = Modifier.fillMaxWidth()) {
                Text("Restore encrypted backup")
            }
        }
    }
}

@Composable
private fun ImportExportSection(
    title: String,
    description: String,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodySmall)
        Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) { Text("Export") }
        OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) { Text("Import") }
    }
}
