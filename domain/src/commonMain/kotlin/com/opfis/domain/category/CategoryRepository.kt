package com.opfis.domain.category

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>

    suspend fun upsert(category: Category)

    suspend fun delete(id: String)
}
