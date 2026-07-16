package com.opfis.app.io

import androidx.compose.runtime.Composable

/** Result of a user-driven "save to a chosen destination" attempt. */
sealed interface FileSaveResult {
    data object Success : FileSaveResult

    data object Cancelled : FileSaveResult

    data class Error(
        val message: String,
    ) : FileSaveResult
}

/**
 * Opens the platform's native "save file" dialog (ROADMAP Phase 9,
 * export flows). Returns a launcher; invoking it with a suggested file
 * name writes [content] to wherever the user chooses. One instance is
 * needed per [mimeType] a screen exports (Android's `CreateDocument`
 * contract fixes its mime type at composition time).
 */
@Composable
expect fun rememberFileSaverLauncher(
    mimeType: String,
    onResult: (FileSaveResult) -> Unit,
): (suggestedFileName: String, content: ByteArray) -> Unit
