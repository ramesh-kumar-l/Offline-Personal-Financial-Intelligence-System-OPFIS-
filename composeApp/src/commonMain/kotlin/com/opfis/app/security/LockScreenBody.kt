package com.opfis.app.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.app.theme.OpfisColors

/** Renders [LockScreen]'s layout: a locked-state message and the unlock action for its current attempt state. */
@Composable
internal fun LockScreenBody(
    authFailed: Boolean,
    biometricUnavailable: Boolean,
    onUnlockTap: () -> Unit,
    onManualConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🔒", style = MaterialTheme.typography.displayMedium)
        Text("OPFIS is locked", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Your financial data stays encrypted until you unlock the app.",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (authFailed) {
            Text(
                "Authentication failed - try again.",
                color = OpfisColors.Error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (biometricUnavailable) {
            Text(
                "Biometric authentication is not available on this device.",
                color = OpfisColors.Warning,
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onManualConfirm) { Text("Confirm to unlock") }
        } else {
            Button(onClick = onUnlockTap) { Text("Unlock") }
        }
    }
}
