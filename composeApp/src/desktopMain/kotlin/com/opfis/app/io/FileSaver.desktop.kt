package com.opfis.app.io

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberFileSaverLauncher(
    mimeType: String,
    onResult: (FileSaveResult) -> Unit,
): (String, ByteArray) -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return { suggestedFileName, content ->
        val dialog = FileDialog(null as Frame?, "Save File", FileDialog.SAVE)
        dialog.file = suggestedFileName
        dialog.isVisible = true
        val directory = dialog.directory
        val fileName = dialog.file
        if (directory != null && fileName != null) {
            File(directory, fileName).writeBytes(content)
            currentOnResult.value(FileSaveResult.Success)
        } else {
            currentOnResult.value(FileSaveResult.Cancelled)
        }
    }
}
