package com.opfis.app.document

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files

@Composable
actual fun rememberDocumentPickerLauncher(onPicked: (PickedFile) -> Unit): () -> Unit {
    val currentOnPicked = rememberUpdatedState(onPicked)
    return {
        val dialog = FileDialog(null as Frame?, "Import Document", FileDialog.LOAD)
        dialog.isVisible = true
        val directory = dialog.directory
        val fileName = dialog.file
        if (directory != null && fileName != null) {
            val file = File(directory, fileName)
            val mimeType = Files.probeContentType(file.toPath()) ?: "application/octet-stream"
            currentOnPicked.value(PickedFile(fileName = file.name, mimeType = mimeType, bytes = file.readBytes()))
        }
    }
}
