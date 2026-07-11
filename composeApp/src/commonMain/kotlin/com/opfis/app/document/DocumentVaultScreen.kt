package com.opfis.app.document

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.opfis.domain.document.DocumentType
import com.opfis.domain.document.usecase.DeleteDocumentUseCase
import com.opfis.domain.document.usecase.ImportDocumentRequest
import com.opfis.domain.document.usecase.ImportDocumentUseCase
import com.opfis.domain.document.usecase.LinkDocumentToTransactionUseCase
import com.opfis.domain.document.usecase.ObserveDocumentsUseCase
import com.opfis.domain.transaction.usecase.ObserveTransactionsUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Receipt vault (ROADMAP Phase 5): imported PDFs/images with OCR/PDF
 * extracted text, optionally linked to a transaction. State/wiring
 * live here; [DocumentVaultScreenBody] renders the layout.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun DocumentVaultScreen() {
    val observeDocuments = koinInject<ObserveDocumentsUseCase>()
    val observeTransactions = koinInject<ObserveTransactionsUseCase>()
    val importDocument = koinInject<ImportDocumentUseCase>()
    val linkDocument = koinInject<LinkDocumentToTransactionUseCase>()
    val deleteDocument = koinInject<DeleteDocumentUseCase>()
    val scope = rememberCoroutineScope()

    val documents by remember { observeDocuments() }.collectAsState(initial = emptyList())
    val transactions by remember { observeTransactions() }.collectAsState(initial = emptyList())

    val launchPicker =
        rememberDocumentPickerLauncher { picked ->
            scope.launch {
                importDocument(
                    ImportDocumentRequest(
                        id = Uuid.random().toString(),
                        fileName = picked.fileName,
                        bytes = picked.bytes,
                        mimeType = picked.mimeType,
                        documentType = DocumentType.OTHER,
                        linkedTransactionId = null,
                        now = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            }
        }

    Scaffold(topBar = { TopAppBar(title = { Text("Receipt Vault") }) }) { padding ->
        DocumentVaultScreenBody(
            padding = padding,
            documents = documents,
            transactions = transactions,
            actions =
                DocumentVaultActions(
                    onImportClick = launchPicker,
                    onLinkTransaction = { documentId, transactionId ->
                        scope.launch { linkDocument(documentId, transactionId) }
                    },
                    onDelete = { document -> scope.launch { deleteDocument(document.id, document.storagePath) } },
                ),
        )
    }
}
