package com.opfis.domain.document.usecase

import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentRepository
import kotlinx.coroutines.flow.Flow

class ObserveDocumentsUseCase(
    private val repository: DocumentRepository,
) {
    operator fun invoke(): Flow<List<Document>> = repository.observeAll()
}
