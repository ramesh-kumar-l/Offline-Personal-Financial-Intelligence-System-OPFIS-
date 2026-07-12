package com.opfis.app.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.opfis.domain.audit.AuditEventType
import com.opfis.domain.audit.AuditLogEntry
import com.opfis.domain.audit.usecase.RecordAuditEventUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Full-screen gate shown while [AppLockState.isLocked] (ROADMAP Phase
 * 8, "Biometrics" + "Auto-lock"). Every unlock attempt - successful,
 * failed, or the no-hardware manual fallback - is written to the audit
 * log. State/wiring live here; [LockScreenBody] renders the layout.
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val recordAuditEvent = koinInject<RecordAuditEventUseCase>()
    val scope = rememberCoroutineScope()

    var biometricUnavailable by remember { mutableStateOf(false) }
    var authFailed by remember { mutableStateOf(false) }

    val launchBiometricAuth =
        rememberBiometricAuthLauncher { result ->
            when (result) {
                is BiometricAuthResult.Success -> {
                    authFailed = false
                    unlockAndAudit(scope, recordAuditEvent, "Unlocked via biometric authentication", onUnlocked)
                }
                is BiometricAuthResult.Failed, is BiometricAuthResult.Error -> {
                    authFailed = true
                    scope.launch {
                        recordAuditEvent(
                            auditEntry(AuditEventType.APP_UNLOCK_FAILED, "Biometric authentication failed"),
                        )
                    }
                }
                is BiometricAuthResult.NotAvailable -> {
                    biometricUnavailable = true
                }
            }
        }

    LockScreenBody(
        authFailed = authFailed,
        biometricUnavailable = biometricUnavailable,
        onUnlockTap = {
            authFailed = false
            launchBiometricAuth()
        },
        onManualConfirm = {
            unlockAndAudit(
                scope,
                recordAuditEvent,
                "Unlocked via manual confirmation (no biometric hardware available on this device)",
                onUnlocked,
            )
        },
    )
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
private fun unlockAndAudit(
    scope: CoroutineScope,
    recordAuditEvent: RecordAuditEventUseCase,
    description: String,
    onUnlocked: () -> Unit,
) {
    scope.launch { recordAuditEvent(auditEntry(AuditEventType.APP_UNLOCKED, description)) }
    onUnlocked()
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
private fun auditEntry(
    eventType: AuditEventType,
    description: String,
): AuditLogEntry =
    AuditLogEntry(
        id = Uuid.random().toString(),
        eventType = eventType,
        description = description,
        occurredAt = Clock.System.now().toEpochMilliseconds(),
    )
