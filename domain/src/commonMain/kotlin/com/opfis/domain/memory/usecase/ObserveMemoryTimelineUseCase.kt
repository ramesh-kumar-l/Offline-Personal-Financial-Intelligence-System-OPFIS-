package com.opfis.domain.memory.usecase

import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventRepository
import kotlinx.coroutines.flow.Flow

/** The chronological financial-memory timeline (ROADMAP Phase 6, "Timeline"). */
class ObserveMemoryTimelineUseCase(
    private val repository: MemoryEventRepository,
) {
    operator fun invoke(): Flow<List<MemoryEvent>> = repository.observeAll()
}
