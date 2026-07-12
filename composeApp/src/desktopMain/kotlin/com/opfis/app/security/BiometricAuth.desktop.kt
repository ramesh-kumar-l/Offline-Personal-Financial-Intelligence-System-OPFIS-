package com.opfis.app.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState

/** No OS-uniform biometric API on the JVM - always resolves to [BiometricAuthResult.NotAvailable]. */
@Composable
actual fun rememberBiometricAuthLauncher(onResult: (BiometricAuthResult) -> Unit): () -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return { currentOnResult.value(BiometricAuthResult.NotAvailable) }
}
