package com.opfis.domain.memory

import com.opfis.domain.entity.EntityType
import kotlinx.coroutines.flow.Flow

interface MemoryEventRepository {
    fun observeAll(): Flow<List<MemoryEvent>>

    fun observeBySubject(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<MemoryEvent>>

    suspend fun upsert(event: MemoryEvent)

    suspend fun delete(id: String)
}
