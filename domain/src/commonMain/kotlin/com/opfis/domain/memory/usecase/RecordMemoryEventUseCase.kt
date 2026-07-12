package com.opfis.domain.memory.usecase

import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventRepository

class RecordMemoryEventUseCase(
    private val repository: MemoryEventRepository,
) {
    suspend operator fun invoke(event: MemoryEvent) = repository.upsert(event)
}
