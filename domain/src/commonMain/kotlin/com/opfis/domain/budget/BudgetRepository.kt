package com.opfis.domain.budget

import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeAll(): Flow<List<Budget>>

    suspend fun upsert(budget: Budget)

    suspend fun delete(id: String)
}
