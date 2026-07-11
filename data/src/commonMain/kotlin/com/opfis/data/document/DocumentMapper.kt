package com.opfis.data.document

import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentType
import com.opfis.data.db.Document as DocumentRow

internal fun toDomainDocument(row: DocumentRow): Document =
    Document(
        id = row.id,
        fileName = row.file_name,
        storagePath = row.storage_path,
        mimeType = row.mime_type,
        documentType = DocumentType.valueOf(row.document_type),
        extractedText = row.extracted_text,
        linkedTransactionId = row.linked_transaction_id,
        importedAt = row.imported_at,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
    )
