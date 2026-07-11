package com.opfis.domain.document.usecase

import com.opfis.domain.document.DocumentRepository

/** Attaches (or detaches, when [transactionId] is null) a document to a transaction. */
class LinkDocumentToTransactionUseCase(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(
        documentId: String,
        transactionId: String?,
    ) = repository.linkToTransaction(documentId, transactionId)
}
