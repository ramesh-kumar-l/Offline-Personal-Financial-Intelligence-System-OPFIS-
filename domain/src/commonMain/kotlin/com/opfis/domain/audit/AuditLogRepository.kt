package com.opfis.domain.audit

import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun observeAll(): Flow<List<AuditLogEntry>>

    suspend fun record(entry: AuditLogEntry)
}
