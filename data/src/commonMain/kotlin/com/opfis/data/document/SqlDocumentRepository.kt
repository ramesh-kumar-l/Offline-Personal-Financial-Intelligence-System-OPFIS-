package com.opfis.data.document

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SqlDocumentRepository(
    private val database: OpfisDatabase,
) : DocumentRepository {
    override fun observeAll(): Flow<List<Document>> =
        database.documentQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainDocument) }

    override fun observeByTransaction(transactionId: String): Flow<List<Document>> =
        database.documentQueries
            .selectByTransaction(transactionId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainDocument) }

    override suspend fun upsert(document: Document) {
        val existingVersion =
            database.documentQueries
                .selectById(document.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.documentQueries.insertOrReplace(
            id = document.id,
            file_name = document.fileName,
            storage_path = document.storagePath,
            mime_type = document.mimeType,
            document_type = document.documentType.name,
            extracted_text = document.extractedText,
            linked_transaction_id = document.linkedTransactionId,
            imported_at = document.importedAt,
            created_at = document.createdAt,
            updated_at = document.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.documentQueries.deleteById(id)
    }

    override suspend fun linkToTransaction(
        documentId: String,
        transactionId: String?,
    ) {
        database.documentQueries.updateLinkedTransaction(
            linked_transaction_id = transactionId,
            updated_at = Clock.System.now().toEpochMilliseconds(),
            id = documentId,
        )
    }
}
