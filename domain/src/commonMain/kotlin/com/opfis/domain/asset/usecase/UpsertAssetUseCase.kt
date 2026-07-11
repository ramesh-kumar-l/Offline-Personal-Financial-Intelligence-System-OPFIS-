package com.opfis.domain.asset.usecase

import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetRepository

class UpsertAssetUseCase(
    private val repository: AssetRepository,
) {
    suspend operator fun invoke(asset: Asset) = repository.upsert(asset)
}
