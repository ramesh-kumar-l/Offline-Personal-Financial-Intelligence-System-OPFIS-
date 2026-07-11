package com.opfis.data.db

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Backed by EncryptedSharedPreferences (Android Keystore-wrapped).
 * Phase 8 adds biometric/auto-lock gating on top of this key; it does
 * not replace it (docs/adr/0005).
 */
actual class DatabaseKeyProvider(
    private val context: Context,
) {
    actual fun getOrCreateKey(): CharArray {
        val prefs = securePrefs()
        val existing = prefs.getString(KEY_PREF_NAME, null)
        if (existing != null) {
            return existing.toCharArray()
        }
        val newKey = generateKey()
        prefs.edit().putString(KEY_PREF_NAME, String(newKey)).apply()
        return newKey
    }

    private fun securePrefs() =
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    private fun generateKey(): CharArray {
        val bytes = ByteArray(KEY_LENGTH_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP).toCharArray()
    }

    private companion object {
        const val PREFS_FILE_NAME = "opfis_secure_prefs"
        const val KEY_PREF_NAME = "db_passphrase"
        const val KEY_LENGTH_BYTES = 32
    }
}
