package com.opfis.domain.asset.usecase

import com.opfis.domain.asset.AssetRepository

class DeleteAssetUseCase(
    private val repository: AssetRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
