package com.opfis.domain.document

import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeAll(): Flow<List<Document>>

    fun observeByTransaction(transactionId: String): Flow<List<Document>>

    suspend fun upsert(document: Document)

    suspend fun delete(id: String)

    suspend fun linkToTransaction(
        documentId: String,
        transactionId: String?,
    )
}
