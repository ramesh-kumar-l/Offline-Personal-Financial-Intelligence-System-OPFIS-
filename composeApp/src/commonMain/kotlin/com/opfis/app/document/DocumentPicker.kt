package com.opfis.app.document

import androidx.compose.runtime.Composable

/** A file picked by the platform's native file chooser, ready for [ImportDocumentUseCase]. */
data class PickedFile(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
)

/**
 * Opens the platform's native "open file" dialog (ROADMAP Phase 5, "PDF
 * import" + "Image import"). Returns a launcher callback; invoking it
 * shows the picker, and [onPicked] fires once the user selects a file.
 */
@Composable
expect fun rememberDocumentPickerLauncher(onPicked: (PickedFile) -> Unit): () -> Unit
