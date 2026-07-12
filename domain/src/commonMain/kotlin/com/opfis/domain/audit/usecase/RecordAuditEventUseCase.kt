package com.opfis.domain.audit.usecase

import com.opfis.domain.audit.AuditLogEntry
import com.opfis.domain.audit.AuditLogRepository

class RecordAuditEventUseCase(
    private val repository: AuditLogRepository,
) {
    suspend operator fun invoke(entry: AuditLogEntry) = repository.record(entry)
}
