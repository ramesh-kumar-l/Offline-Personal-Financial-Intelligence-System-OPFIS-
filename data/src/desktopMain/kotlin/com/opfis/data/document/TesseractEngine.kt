package com.opfis.data.document

import net.sourceforge.tess4j.Tesseract
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

/**
 * Lazily extracts the bundled `eng.traineddata`
 * (`desktopMain/resources/tessdata/`) to a real temp directory once per
 * process - Tesseract requires a filesystem path, not a classpath
 * resource. Confirmed working end-to-end in this dev environment
 * (Tess4j's bundled native library loads via JNA with no system
 * Tesseract install required).
 */
internal object TesseractEngine {
    private val tessdataDir: Path by lazy {
        val dir = Files.createTempDirectory("opfis-tessdata")
        TesseractEngine::class.java.getResourceAsStream("/tessdata/eng.traineddata")!!.use { input ->
            Files.copy(input, dir.resolve("eng.traineddata"))
        }
        dir
    }

    fun ocr(image: BufferedImage): String =
        Tesseract()
            .apply { setDatapath(tessdataDir.toAbsolutePath().toString()) }
            .doOCR(image)
            .trim()
}
