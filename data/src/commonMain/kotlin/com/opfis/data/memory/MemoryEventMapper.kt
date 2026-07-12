package com.opfis.data.memory

import com.opfis.data.db.Memory_event
import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventType

internal fun toDomainMemoryEvent(row: Memory_event): MemoryEvent =
    MemoryEvent(
        id = row.id,
        eventType = MemoryEventType.valueOf(row.event_type),
        title = row.title,
        description = row.description,
        subject = toSubjectRef(row.subject_entity_type, row.subject_entity_id),
        occurredAt = row.occurred_at,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
    )

private fun toSubjectRef(
    entityType: String?,
    entityId: String?,
): EntityRef? {
    if (entityType == null || entityId == null) return null
    return EntityRef(EntityType.valueOf(entityType), entityId)
}
