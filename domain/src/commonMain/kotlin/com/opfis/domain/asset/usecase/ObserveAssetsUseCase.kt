package com.opfis.domain.asset.usecase

import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetRepository
import kotlinx.coroutines.flow.Flow

class ObserveAssetsUseCase(
    private val repository: AssetRepository,
) {
    operator fun invoke(): Flow<List<Asset>> = repository.observeAll()
}
