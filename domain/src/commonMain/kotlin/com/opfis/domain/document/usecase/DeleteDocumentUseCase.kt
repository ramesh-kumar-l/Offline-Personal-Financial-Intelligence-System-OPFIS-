package com.opfis.domain.document.usecase

import com.opfis.domain.document.DocumentRepository
import com.opfis.domain.document.DocumentStoragePort

class DeleteDocumentUseCase(
    private val repository: DocumentRepository,
    private val storage: DocumentStoragePort,
) {
    suspend operator fun invoke(
        id: String,
        storagePath: String,
    ) {
        repository.delete(id)
        storage.delete(storagePath)
    }
}
