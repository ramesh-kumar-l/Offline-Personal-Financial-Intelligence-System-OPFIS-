package com.opfis.domain.goal

import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeAll(): Flow<List<Goal>>

    suspend fun upsert(goal: Goal)

    suspend fun delete(id: String)
}
