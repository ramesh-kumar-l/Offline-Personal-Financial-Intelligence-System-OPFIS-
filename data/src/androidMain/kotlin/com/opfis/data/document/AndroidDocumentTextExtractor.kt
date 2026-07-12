package com.opfis.data.document

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.opfis.domain.document.DocumentTextExtractorPort
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val PDF_RENDER_SCALE = 2

/**
 * Android has no bundled PDF text-extraction API (unlike Desktop's
 * PDFBox path), so every PDF page is rendered to a bitmap via
 * [PdfRenderer] and OCR'd. Uses ML Kit's standalone text recognizer
 * (`com.google.mlkit:text-recognition` - model bundled in the app, no
 * Play Services network call at runtime, keeping this fully offline).
 * Not verified on a device/emulator in this environment (no Android
 * runtime available here) - see `05-current-state.md` known gaps,
 * same caveat as the existing SQLCipher Android path.
 */
class AndroidDocumentTextExtractor(
    private val context: Context,
) : DocumentTextExtractorPort {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(
        bytes: ByteArray,
        mimeType: String,
    ): String =
        when {
            mimeType == "application/pdf" -> extractFromPdf(bytes)
            mimeType.startsWith("image/") -> extractFromImage(bytes)
            else -> ""
        }

    private suspend fun extractFromPdf(bytes: ByteArray): String {
        val tempFile = File.createTempFile("opfis-pdf", ".pdf", context.cacheDir)
        return try {
            tempFile.writeBytes(bytes)
            ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer -> recognizeAllPages(renderer) }
            }
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun recognizeAllPages(renderer: PdfRenderer): String {
        val pageTexts = mutableListOf<String>()
        for (index in 0 until renderer.pageCount) {
            renderer.openPage(index).use { page -> pageTexts += recognizeText(renderPage(page)) }
        }
        return pageTexts.joinToString(separator = "\n")
    }

    private fun renderPage(page: PdfRenderer.Page): Bitmap {
        val bitmap =
            Bitmap.createBitmap(
                page.width * PDF_RENDER_SCALE,
                page.height * PDF_RENDER_SCALE,
                Bitmap.Config.ARGB_8888,
            )
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    private suspend fun extractFromImage(bytes: ByteArray): String {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return ""
        return recognizeText(bitmap)
    }

    private suspend fun recognizeText(bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            recognizer
                .process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { continuation.resume(it.text) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
}
