package com.opfis.app.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.domain.audit.AuditLogEntry

/** Renders [SecurityScreen]'s layout: a policy summary card above the audit trail. */
@Composable
internal fun SecurityScreenBody(
    padding: PaddingValues,
    auditLog: List<AuditLogEntry>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SecurityPolicySummaryCard()

        Text("Audit log", style = MaterialTheme.typography.titleMedium)
        if (auditLog.isEmpty()) {
            Text("No security events recorded yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            auditLog.forEach { entry -> AuditLogRow(entry) }
        }
    }
}

@Composable
private fun SecurityPolicySummaryCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Protection", style = MaterialTheme.typography.titleMedium)
            Text(
                "OPFIS locks itself after 5 minutes of inactivity and requires biometric " +
                    "authentication to unlock on devices that support it. Devices without " +
                    "biometric hardware fall back to a manual confirmation, which is also audited.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Your data is always encrypted at rest (SQLCipher); backups inherit the same encryption.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
