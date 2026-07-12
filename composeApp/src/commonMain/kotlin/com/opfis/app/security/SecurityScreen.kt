package com.opfis.app.security

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.opfis.domain.audit.usecase.ObserveAuditLogUseCase
import org.koin.compose.koinInject

/**
 * Security overview (ROADMAP Phase 8): explains the app's biometric/
 * auto-lock behavior and shows the audit trail of unlock and backup
 * events - see `09-security-model.md`. State/wiring live here;
 * [SecurityScreenBody] renders the layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen() {
    val observeAuditLog = koinInject<ObserveAuditLogUseCase>()
    val auditLog by remember { observeAuditLog() }.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Security") }) }) { padding ->
        SecurityScreenBody(padding = padding, auditLog = auditLog)
    }
}
