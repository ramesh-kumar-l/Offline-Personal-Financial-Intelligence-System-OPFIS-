package com.opfis.app.document

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.domain.document.Document
import com.opfis.domain.transaction.Transaction

/** Groups [DocumentVaultScreenBody]'s callbacks to keep the composable's parameter count in check. */
internal data class DocumentVaultActions(
    val onImportClick: () -> Unit,
    val onLinkTransaction: (documentId: String, transactionId: String?) -> Unit,
    val onDelete: (Document) -> Unit,
)

@Composable
internal fun DocumentVaultScreenBody(
    padding: PaddingValues,
    documents: List<Document>,
    transactions: List<Transaction>,
    actions: DocumentVaultActions,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = actions.onImportClick, modifier = Modifier.fillMaxWidth()) {
            Text("Import receipt, statement, or invoice")
        }

        if (documents.isEmpty()) {
            Text("No documents imported yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            documents.forEach { document ->
                DocumentRow(
                    document = document,
                    transactions = transactions,
                    onLinkTransaction = { transactionId -> actions.onLinkTransaction(document.id, transactionId) },
                    onDelete = { actions.onDelete(document) },
                )
            }
        }
    }
}
