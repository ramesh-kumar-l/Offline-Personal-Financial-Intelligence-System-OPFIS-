package com.opfis.domain.document

/**
 * Owns the raw bytes of an imported document on disk (ROADMAP Phase 5).
 * The database only stores the [Document.storagePath] this returns,
 * never the file content itself - kept out of SQLCipher-encrypted rows
 * to avoid bloating the database with large binary blobs.
 */
interface DocumentStoragePort {
    /** [id] namespaces the stored file so two imports of the same [fileName] never collide. */
    suspend fun save(
        id: String,
        fileName: String,
        bytes: ByteArray,
    ): String

    suspend fun read(storagePath: String): ByteArray

    suspend fun delete(storagePath: String)
}
