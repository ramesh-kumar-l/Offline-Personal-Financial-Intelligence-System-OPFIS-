package com.opfis.domain.asset

import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun observeAll(): Flow<List<Asset>>

    suspend fun upsert(asset: Asset)

    suspend fun delete(id: String)
}
