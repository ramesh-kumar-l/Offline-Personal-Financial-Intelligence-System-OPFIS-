package com.opfis.app.io

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFileSaverLauncher(
    mimeType: String,
    onResult: (FileSaveResult) -> Unit,
): (String, ByteArray) -> Unit {
    val context = LocalContext.current
    val currentOnResult = rememberUpdatedState(onResult)
    var pendingContent by remember { mutableStateOf<ByteArray?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mimeType)) { uri ->
            val content = pendingContent
            if (uri != null && content != null) {
                context.contentResolver.openOutputStream(uri)?.use { it.write(content) }
                currentOnResult.value(FileSaveResult.Success)
            } else {
                currentOnResult.value(FileSaveResult.Cancelled)
            }
            pendingContent = null
        }
    return { suggestedFileName, content ->
        pendingContent = content
        launcher.launch(suggestedFileName)
    }
}
