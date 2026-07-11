package com.opfis.data.document

import com.opfis.domain.document.DocumentTextExtractorPort
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

private const val MIN_EMBEDDED_TEXT_LENGTH = 20
private const val OCR_RENDER_DPI = 200f

/**
 * Reads a digital PDF's embedded text layer directly (Apache PDFBox);
 * falls back to rendering each page and OCR'ing it (Tesseract, via
 * [TesseractEngine]) when the PDF turns out to be a scanned image with
 * no usable text layer. Plain images always go straight to OCR.
 */
class DesktopDocumentTextExtractor : DocumentTextExtractorPort {
    override suspend fun extractText(
        bytes: ByteArray,
        mimeType: String,
    ): String =
        when {
            mimeType == "application/pdf" -> extractFromPdf(bytes)
            mimeType.startsWith("image/") -> extractFromImage(bytes)
            else -> ""
        }

    private fun extractFromPdf(bytes: ByteArray): String =
        Loader.loadPDF(bytes).use { document ->
            val embeddedText = PDFTextStripper().getText(document).trim()
            if (embeddedText.length >= MIN_EMBEDDED_TEXT_LENGTH) {
                embeddedText
            } else {
                val renderer = PDFRenderer(document)
                (0 until document.numberOfPages)
                    .joinToString(separator = "\n") { pageIndex ->
                        TesseractEngine.ocr(renderer.renderImageWithDPI(pageIndex, OCR_RENDER_DPI))
                    }.trim()
            }
        }

    private fun extractFromImage(bytes: ByteArray): String {
        val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: return ""
        return TesseractEngine.ocr(image)
    }
}
