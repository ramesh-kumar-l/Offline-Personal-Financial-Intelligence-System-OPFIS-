package com.opfis.data.document

import android.content.Context
import com.opfis.domain.document.DocumentStoragePort
import java.io.File

/** Stores document bytes under the app's private `filesDir/documents/`. */
class AndroidDocumentStorage(
    private val context: Context,
) : DocumentStoragePort {
    override suspend fun save(
        id: String,
        fileName: String,
        bytes: ByteArray,
    ): String {
        val documentsDir = File(context.filesDir, "documents").apply { mkdirs() }
        val target = File(documentsDir, "$id-$fileName")
        target.writeBytes(bytes)
        return target.absolutePath
    }

    override suspend fun read(storagePath: String): ByteArray = File(storagePath).readBytes()

    override suspend fun delete(storagePath: String) {
        File(storagePath).delete()
    }
}
