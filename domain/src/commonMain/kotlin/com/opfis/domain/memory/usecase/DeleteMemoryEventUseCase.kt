package com.opfis.domain.memory.usecase

import com.opfis.domain.memory.MemoryEventRepository

class DeleteMemoryEventUseCase(
    private val repository: MemoryEventRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
