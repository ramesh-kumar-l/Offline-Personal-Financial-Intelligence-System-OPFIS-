package com.opfis.app.io

import androidx.compose.runtime.Composable
import java.io.File

@Composable
actual fun rememberTempFilePathFactory(): (String, String) -> String =
    { prefix, suffix -> File.createTempFile(prefix, suffix).absolutePath }

actual fun writeFileBytes(
    path: String,
    bytes: ByteArray,
) {
    File(path).writeBytes(bytes)
}

actual fun readFileBytes(path: String): ByteArray = File(path).readBytes()
