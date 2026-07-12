package com.opfis.app.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val ALLOWED_AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

@Composable
actual fun rememberBiometricAuthLauncher(onResult: (BiometricAuthResult) -> Unit): () -> Unit {
    val activity = LocalContext.current as? FragmentActivity
    val currentOnResult = rememberUpdatedState(onResult)
    return {
        if (activity == null) {
            currentOnResult.value(BiometricAuthResult.NotAvailable)
        } else {
            launchBiometricPrompt(activity, currentOnResult.value)
        }
    }
}

private fun launchBiometricPrompt(
    activity: FragmentActivity,
    onResult: (BiometricAuthResult) -> Unit,
) {
    if (BiometricManager.from(activity).canAuthenticate(ALLOWED_AUTHENTICATORS) != BiometricManager.BIOMETRIC_SUCCESS) {
        onResult(BiometricAuthResult.NotAvailable)
        return
    }
    val promptInfo =
        BiometricPrompt.PromptInfo
            .Builder()
            .setTitle("Unlock OPFIS")
            .setSubtitle("Authenticate to access your financial data")
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()
    val callback =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricAuthResult.Success)
            }

            override fun onAuthenticationFailed() {
                onResult(BiometricAuthResult.Failed)
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence,
            ) {
                onResult(BiometricAuthResult.Error(errString.toString()))
            }
        }
    BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback).authenticate(promptInfo)
}
