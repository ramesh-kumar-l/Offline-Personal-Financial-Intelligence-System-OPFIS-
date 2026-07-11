package com.opfis.domain.document.usecase

import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentRepository
import kotlinx.coroutines.flow.Flow

/** Powers the "receipt vault" view on a single transaction. */
class ObserveDocumentsForTransactionUseCase(
    private val repository: DocumentRepository,
) {
    operator fun invoke(transactionId: String): Flow<List<Document>> = repository.observeByTransaction(transactionId)
}
