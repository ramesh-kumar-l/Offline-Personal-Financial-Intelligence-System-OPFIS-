package com.opfis.data.db

/**
 * Supplies the per-install SQLCipher passphrase, generating and
 * persisting one on first use. See docs/adr/0005 for the platform key
 * storage strategy and its Phase 8 hardening follow-up.
 */
expect class DatabaseKeyProvider {
    fun getOrCreateKey(): CharArray
}
