package com.opfis.domain.document

/**
 * Extracts searchable text from an imported file (ROADMAP Phase 5,
 * "OCR" + "PDF import"). Implemented per-platform in `:data`: a digital
 * PDF's embedded text is read directly, a scanned PDF page or a plain
 * image is OCR'd. Returns an empty string (never throws) when no text
 * can be recovered, so import always succeeds and the document is
 * simply not text-searchable.
 */
interface DocumentTextExtractorPort {
    suspend fun extractText(
        bytes: ByteArray,
        mimeType: String,
    ): String
}
