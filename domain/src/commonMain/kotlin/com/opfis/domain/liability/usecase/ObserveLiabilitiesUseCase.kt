package com.opfis.domain.liability.usecase

import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityRepository
import kotlinx.coroutines.flow.Flow

class ObserveLiabilitiesUseCase(
    private val repository: LiabilityRepository,
) {
    operator fun invoke(): Flow<List<Liability>> = repository.observeAll()
}
