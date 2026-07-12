package com.opfis.domain.audit.usecase

import com.opfis.domain.audit.AuditLogEntry
import com.opfis.domain.audit.AuditLogRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuditLogUseCase(
    private val repository: AuditLogRepository,
) {
    operator fun invoke(): Flow<List<AuditLogEntry>> = repository.observeAll()
}
