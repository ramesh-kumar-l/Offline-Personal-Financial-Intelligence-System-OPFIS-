package com.opfis.domain.audit

/**
 * One immutable, append-only security event (ROADMAP Phase 8, "Audit
 * log"). Never updated or deleted after creation - an audit trail must
 * stay trustworthy, so `AuditLogRepository` exposes no `delete`/`update`.
 */
data class AuditLogEntry(
    val id: String,
    val eventType: AuditEventType,
    val description: String,
    val occurredAt: Long,
)

enum class AuditEventType {
    APP_UNLOCKED,
    APP_UNLOCK_FAILED,
    BACKUP_EXPORTED,
    BACKUP_RESTORED,
    DATA_EXPORTED,
    DATA_IMPORTED,
}
