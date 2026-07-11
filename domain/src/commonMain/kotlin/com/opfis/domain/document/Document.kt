package com.opfis.domain.document

/**
 * An imported PDF or image file (ROADMAP Phase 5, "Document
 * Intelligence"): a receipt, statement, or invoice with OCR/PDF-extracted
 * [extractedText] so it participates in global search (see
 * `14-search-engine.md`). [storagePath] is a platform-managed location
 * ([DocumentStoragePort] owns the raw bytes); the database only ever
 * holds metadata and extracted text, never the file itself. Optionally
 * linked to one [com.opfis.domain.transaction.Transaction] (the
 * "receipt vault" use case).
 */
data class Document(
    val id: String,
    val fileName: String,
    val storagePath: String,
    val mimeType: String,
    val documentType: DocumentType,
    val extractedText: String,
    val linkedTransactionId: String? = null,
    val importedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(fileName.isNotBlank()) { "fileName must not be blank" }
        require(storagePath.isNotBlank()) { "storagePath must not be blank" }
        require(mimeType.isNotBlank()) { "mimeType must not be blank" }
    }
}

enum class DocumentType {
    RECEIPT,
    STATEMENT,
    INVOICE,
    OTHER,
}
