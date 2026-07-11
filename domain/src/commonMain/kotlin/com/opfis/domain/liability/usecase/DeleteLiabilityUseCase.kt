package com.opfis.domain.liability.usecase

import com.opfis.domain.liability.LiabilityRepository

class DeleteLiabilityUseCase(
    private val repository: LiabilityRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
