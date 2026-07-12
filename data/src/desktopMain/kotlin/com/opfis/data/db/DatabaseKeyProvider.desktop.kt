package com.opfis.data.db

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.AclEntry
import java.nio.file.attribute.AclEntryPermission
import java.nio.file.attribute.AclEntryType
import java.nio.file.attribute.AclFileAttributeView
import java.nio.file.attribute.DosFileAttributeView
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions
import java.security.SecureRandom
import java.util.Base64

/**
 * Stores a randomly generated key in a file under the app's private
 * data directory, restricted to the owning OS account (ROADMAP Phase
 * 8, "Backup encryption" hardening - a backup's confidentiality is only
 * as strong as the key that produced it). There is still no OS-uniform
 * Desktop equivalent of Android Keystore; full OS keychain/DPAPI
 * integration remains explicit follow-up work (docs/adr/0005) - this
 * narrows the weak point ADR 0005 flagged (any process on the machine
 * could read the key file) to the owning user account only, via
 * whichever of POSIX permissions / Windows ACLs the filesystem
 * supports.
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
        restrictToOwner(keyFile)
        return key.toCharArray()
    }

    private fun restrictToOwner(keyFile: Path) {
        runCatching {
            val posixView = Files.getFileAttributeView(keyFile, PosixFileAttributeView::class.java)
            if (posixView != null) {
                posixView.setPermissions(PosixFilePermissions.fromString("rw-------"))
                return@runCatching
            }
            restrictWindowsAcl(keyFile)
        }
    }

    private fun restrictWindowsAcl(keyFile: Path) {
        Files.getFileAttributeView(keyFile, DosFileAttributeView::class.java)?.setHidden(true)
        val aclView = Files.getFileAttributeView(keyFile, AclFileAttributeView::class.java) ?: return
        val ownerEntry =
            AclEntry
                .newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(aclView.owner)
                .setPermissions(AclEntryPermission.entries.toSet())
                .build()
        aclView.acl = listOf(ownerEntry)
    }

    private companion object {
        const val KEY_FILE_NAME = ".opfis_db_key"
        const val KEY_LENGTH_BYTES = 32
    }
}
