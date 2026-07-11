package com.opfis.domain.liability.usecase

import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityRepository

class UpsertLiabilityUseCase(
    private val repository: LiabilityRepository,
) {
    suspend operator fun invoke(liability: Liability) = repository.upsert(liability)
}
