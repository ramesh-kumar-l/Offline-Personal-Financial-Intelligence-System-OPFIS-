package com.opfis.domain.memory.usecase

import com.opfis.domain.entity.EntityType
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventRepository
import kotlinx.coroutines.flow.Flow

/** Memory events attached to one [subject] entity, e.g. an Account's or Goal's own history. */
class ObserveMemoryEventsForEntityUseCase(
    private val repository: MemoryEventRepository,
) {
    operator fun invoke(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<MemoryEvent>> = repository.observeBySubject(entityType, entityId)
}
