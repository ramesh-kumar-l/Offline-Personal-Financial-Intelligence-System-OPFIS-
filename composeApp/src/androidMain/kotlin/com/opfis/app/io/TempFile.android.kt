package com.opfis.app.io

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
actual fun rememberTempFilePathFactory(): (String, String) -> String {
    val context = LocalContext.current
    return { prefix, suffix -> File.createTempFile(prefix, suffix, context.cacheDir).absolutePath }
}

actual fun writeFileBytes(
    path: String,
    bytes: ByteArray,
) {
    File(path).writeBytes(bytes)
}

actual fun readFileBytes(path: String): ByteArray = File(path).readBytes()
