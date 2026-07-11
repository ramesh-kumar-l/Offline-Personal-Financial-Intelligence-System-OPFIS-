package com.opfis.data.db

import java.nio.file.Files
import java.nio.file.Path
import java.security.SecureRandom
import java.util.Base64

/**
 * Stores a randomly generated key in a file under the app's private
 * data directory. There is no OS-uniform Desktop equivalent of Android
 * Keystore; hardening this with OS keychain/DPAPI integration is
 * explicit Phase 8 follow-up work (docs/adr/0005), not silently
 * deferred.
 */
actual class DatabaseKeyProvider(
    private val appDataDirectory: Path,
) {
    actual fun getOrCreateKey(): CharArray {
        Files.createDirectories(appDataDirectory)
        val keyFile = appDataDirectory.resolve(KEY_FILE_NAME)
        if (Files.exists(keyFile)) {
            return Files.readString(keyFile).toCharArray()
        }
        val bytes = ByteArray(KEY_LENGTH_BYTES)
        SecureRandom().nextBytes(bytes)
        val key = Base64.getEncoder().withoutPadding().encodeToString(bytes)
        Files.writeString(keyFile, key)
        return key.toCharArray()
    }

    private companion object {
        const val KEY_FILE_NAME = ".opfis_db_key"
        const val KEY_LENGTH_BYTES = 32
    }
}
