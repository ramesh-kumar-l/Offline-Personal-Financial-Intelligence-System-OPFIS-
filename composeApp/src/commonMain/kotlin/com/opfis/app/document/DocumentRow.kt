package com.opfis.app.document

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.document.Document
import com.opfis.domain.transaction.Transaction

private const val EXTRACTED_TEXT_PREVIEW_LENGTH = 120

/** One imported document (ROADMAP Phase 5, "Receipt vault"): metadata, extracted-text preview, link/delete actions. */
@Composable
internal fun DocumentRow(
    document: Document,
    transactions: List<Transaction>,
    onLinkTransaction: (transactionId: String?) -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val linkedTransaction = transactions.find { it.id == document.linkedTransactionId }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("📄 ${document.fileName}", style = MaterialTheme.typography.bodyLarge)
        Text(
            document.documentType.name,
            style = MaterialTheme.typography.bodySmall,
            color = OpfisColors.InformationNeutralBlue,
        )
        if (document.extractedText.isNotBlank()) {
            Text(document.extractedText.take(EXTRACTED_TEXT_PREVIEW_LENGTH), style = MaterialTheme.typography.bodySmall)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (linkedTransaction != null) {
                TextButton(onClick = { onLinkTransaction(null) }) {
                    Text("Linked: ${linkedTransaction.description.ifBlank { linkedTransaction.type.name }} ✕")
                }
            } else {
                TextButton(onClick = { menuExpanded = true }) { Text("Link to transaction") }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    transactions.forEach { transaction ->
                        DropdownMenuItem(
                            text = { Text(transaction.description.ifBlank { transaction.type.name }) },
                            onClick = {
                                onLinkTransaction(transaction.id)
                                menuExpanded = false
                            },
                        )
                    }
                }
            }
            TextButton(onClick = onDelete) {
                Text("Delete", color = OpfisColors.Error)
            }
        }
    }
}
