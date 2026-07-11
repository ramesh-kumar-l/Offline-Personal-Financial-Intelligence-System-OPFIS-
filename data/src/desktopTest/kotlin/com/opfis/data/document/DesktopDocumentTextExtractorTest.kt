package com.opfis.data.document

import kotlinx.coroutines.test.runTest
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopDocumentTextExtractorTest {
    @Test
    fun `reads the embedded text layer of a digital PDF directly`() =
        runTest {
            val bytes = digitalPdfBytes("Invoice total: 128.50")

            val text = DesktopDocumentTextExtractor().extractText(bytes, "application/pdf")

            assertTrue(text.contains("Invoice total"))
        }

    @Test
    fun `OCRs a plain image`() =
        runTest {
            val bytes = renderedTextImageBytes("RECEIPT 42")

            val text = DesktopDocumentTextExtractor().extractText(bytes, "image/png")

            assertTrue(text.contains("RECEIPT", ignoreCase = true) || text.contains("42"))
        }

    @Test
    fun `falls back to OCR for a scanned PDF with no text layer`() =
        runTest {
            val bytes = scannedPdfBytes("SCANNED 99")

            val text = DesktopDocumentTextExtractor().extractText(bytes, "application/pdf")

            assertTrue(text.contains("SCANNED", ignoreCase = true) || text.contains("99"))
        }

    private fun digitalPdfBytes(text: String): ByteArray =
        PDDocument().use { document ->
            val page = PDPage(PDRectangle.LETTER)
            document.addPage(page)
            PDPageContentStream(document, page).use { stream ->
                stream.beginText()
                stream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 18f)
                stream.newLineAtOffset(50f, 700f)
                stream.showText(text)
                stream.endText()
            }
            val output = ByteArrayOutputStream()
            document.save(output)
            output.toByteArray()
        }

    private fun scannedPdfBytes(text: String): ByteArray =
        PDDocument().use { document ->
            val page = PDPage(PDRectangle(300f, 100f))
            document.addPage(page)
            val image = LosslessFactory.createFromImage(document, renderedTextImage(text))
            PDPageContentStream(document, page).use { stream ->
                stream.drawImage(image, 0f, 0f, 300f, 100f)
            }
            val output = ByteArrayOutputStream()
            document.save(output)
            output.toByteArray()
        }

    private fun renderedTextImageBytes(text: String): ByteArray {
        val output = ByteArrayOutputStream()
        ImageIO.write(renderedTextImage(text), "png", output)
        return output.toByteArray()
    }

    private fun renderedTextImage(text: String): BufferedImage {
        val image = BufferedImage(300, 100, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, 300, 100)
        graphics.color = Color.BLACK
        graphics.font = Font("SansSerif", Font.PLAIN, 32)
        graphics.drawString(text, 10, 60)
        graphics.dispose()
        return image
    }
}
