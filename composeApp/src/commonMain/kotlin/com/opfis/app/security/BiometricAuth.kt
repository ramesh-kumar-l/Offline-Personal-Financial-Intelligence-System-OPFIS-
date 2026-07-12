package com.opfis.app.security

import androidx.compose.runtime.Composable

/** Outcome of a [rememberBiometricAuthLauncher] attempt (ROADMAP Phase 8, "Biometrics"). */
sealed interface BiometricAuthResult {
    data object Success : BiometricAuthResult

    data object Failed : BiometricAuthResult

    data class Error(
        val message: String,
    ) : BiometricAuthResult

    /** No biometric hardware/enrollment on this platform - caller must offer a fallback unlock path. */
    data object NotAvailable : BiometricAuthResult
}

/**
 * Requests biometric authentication to unlock the app ([LockScreen]).
 * Returns a launcher callback; invoking it shows the platform's
 * biometric prompt (Android) or resolves immediately to [NotAvailable]
 * (Desktop - no OS-uniform biometric API on the JVM, a documented gap,
 * see `09-security-model.md`).
 */
@Composable
expect fun rememberBiometricAuthLauncher(onResult: (BiometricAuthResult) -> Unit): () -> Unit
