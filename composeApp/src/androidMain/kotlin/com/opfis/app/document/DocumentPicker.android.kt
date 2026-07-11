package com.opfis.app.document

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDocumentPickerLauncher(onPicked: (PickedFile) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnPicked = rememberUpdatedState(onPicked)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                readPickedFile(context, uri)?.let(currentOnPicked.value)
            }
        }
    return { launcher.launch("*/*") }
}

private fun readPickedFile(
    context: Context,
    uri: Uri,
): PickedFile? {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
    val fileName = queryDisplayName(context, uri) ?: "document"
    return PickedFile(fileName = fileName, mimeType = mimeType, bytes = bytes)
}

private fun queryDisplayName(
    context: Context,
    uri: Uri,
): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) return cursor.getString(nameIndex)
    }
    return null
}
