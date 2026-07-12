package com.opfis.data.audit

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.audit.AuditEventType
import com.opfis.domain.audit.AuditLogEntry
import com.opfis.domain.audit.AuditLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlAuditLogRepository(
    private val database: OpfisDatabase,
) : AuditLogRepository {
    override fun observeAll(): Flow<List<AuditLogEntry>> =
        database.auditLogQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows ->
                rows.map { row ->
                    AuditLogEntry(
                        id = row.id,
                        eventType = AuditEventType.valueOf(row.event_type),
                        description = row.description,
                        occurredAt = row.occurred_at,
                    )
                }
            }

    override suspend fun record(entry: AuditLogEntry) {
        database.auditLogQueries.insert(
            id = entry.id,
            event_type = entry.eventType.name,
            description = entry.description,
            occurred_at = entry.occurredAt,
        )
    }
}
