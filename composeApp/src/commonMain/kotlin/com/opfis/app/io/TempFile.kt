package com.opfis.app.io

import androidx.compose.runtime.Composable

/**
 * A writable, platform-appropriate temporary file path factory
 * (ROADMAP Phase 9 staging step: `VACUUM INTO`/backup restore need a
 * real filesystem path, which Android's SAF `content://` Uris don't
 * provide directly - export/import stage through a temp file first).
 */
@Composable
expect fun rememberTempFilePathFactory(): (prefix: String, suffix: String) -> String

/** Writes [bytes] to the plain filesystem [path] (not a `content://` Uri). */
expect fun writeFileBytes(
    path: String,
    bytes: ByteArray,
)

/** Reads the plain filesystem [path] (not a `content://` Uri) back into memory. */
expect fun readFileBytes(path: String): ByteArray
