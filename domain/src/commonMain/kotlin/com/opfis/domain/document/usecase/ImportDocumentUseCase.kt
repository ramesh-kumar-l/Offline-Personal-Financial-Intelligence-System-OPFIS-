package com.opfis.domain.document.usecase

import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentRepository
import com.opfis.domain.document.DocumentStoragePort
import com.opfis.domain.document.DocumentTextExtractorPort
import com.opfis.domain.document.DocumentType

/** Caller-supplied fields for [ImportDocumentUseCase] (id/timestamp follow the Phase 4 `Tag` convention). */
data class ImportDocumentRequest(
    val id: String,
    val fileName: String,
    val bytes: ByteArray,
    val mimeType: String,
    val documentType: DocumentType,
    val linkedTransactionId: String?,
    val now: Long,
)

/**
 * Saves an imported file's bytes, extracts its text (PDF text layer or
 * OCR), and persists the resulting [Document] (ROADMAP Phase 5).
 */
class ImportDocumentUseCase(
    private val documentRepository: DocumentRepository,
    private val storage: DocumentStoragePort,
    private val textExtractor: DocumentTextExtractorPort,
) {
    suspend operator fun invoke(request: ImportDocumentRequest): Document {
        val storagePath = storage.save(request.id, request.fileName, request.bytes)
        val extractedText = textExtractor.extractText(request.bytes, request.mimeType)
        val document =
            Document(
                id = request.id,
                fileName = request.fileName,
                storagePath = storagePath,
                mimeType = request.mimeType,
                documentType = request.documentType,
                extractedText = extractedText,
                linkedTransactionId = request.linkedTransactionId,
                importedAt = request.now,
                createdAt = request.now,
                updatedAt = request.now,
            )
        documentRepository.upsert(document)
        return document
    }
}
