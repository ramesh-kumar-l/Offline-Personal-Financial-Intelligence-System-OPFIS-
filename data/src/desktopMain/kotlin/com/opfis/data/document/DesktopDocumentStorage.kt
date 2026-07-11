package com.opfis.data.document

import com.opfis.domain.document.DocumentStoragePort
import java.nio.file.Files
import java.nio.file.Path

/** Stores document bytes under `<databaseDirectory>/documents/` (see `desktopDataModule`). */
class DesktopDocumentStorage(
    private val baseDirectory: Path,
) : DocumentStoragePort {
    override suspend fun save(
        id: String,
        fileName: String,
        bytes: ByteArray,
    ): String {
        val documentsDir = baseDirectory.resolve("documents")
        Files.createDirectories(documentsDir)
        val target = documentsDir.resolve("$id-$fileName")
        Files.write(target, bytes)
        return target.toAbsolutePath().toString()
    }

    override suspend fun read(storagePath: String): ByteArray = Files.readAllBytes(Path.of(storagePath))

    override suspend fun delete(storagePath: String) {
        Files.deleteIfExists(Path.of(storagePath))
    }
}
