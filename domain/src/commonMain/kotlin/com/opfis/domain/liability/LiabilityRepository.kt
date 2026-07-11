package com.opfis.domain.liability

import kotlinx.coroutines.flow.Flow

interface LiabilityRepository {
    fun observeAll(): Flow<List<Liability>>

    suspend fun upsert(liability: Liability)

    suspend fun delete(id: String)
}
