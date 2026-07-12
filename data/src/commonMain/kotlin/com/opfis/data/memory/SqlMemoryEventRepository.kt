package com.opfis.data.memory

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.entity.EntityType
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlMemoryEventRepository(
    private val database: OpfisDatabase,
) : MemoryEventRepository {
    override fun observeAll(): Flow<List<MemoryEvent>> =
        database.memoryEventQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainMemoryEvent) }

    override fun observeBySubject(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<MemoryEvent>> =
        database.memoryEventQueries
            .selectBySubject(entityType.name, entityId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainMemoryEvent) }

    override suspend fun upsert(event: MemoryEvent) {
        val existingVersion =
            database.memoryEventQueries
                .selectById(event.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.memoryEventQueries.insertOrReplace(
            id = event.id,
            event_type = event.eventType.name,
            title = event.title,
            description = event.description,
            subject_entity_type = event.subject?.entityType?.name,
            subject_entity_id = event.subject?.entityId,
            occurred_at = event.occurredAt,
            created_at = event.createdAt,
            updated_at = event.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.memoryEventQueries.deleteById(id)
    }
}
