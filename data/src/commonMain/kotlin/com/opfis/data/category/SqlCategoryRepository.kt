package com.opfis.data.category

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlCategoryRepository(
    private val database: OpfisDatabase,
) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> =
        database.categoryQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainCategory) }

    override suspend fun upsert(category: Category) {
        val existingVersion =
            database.categoryQueries
                .selectById(category.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.categoryQueries.insertOrReplace(
            id = category.id,
            name = category.name,
            type = category.type.name,
            parent_id = category.parentId,
            created_at = category.createdAt,
            updated_at = category.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.categoryQueries.deleteById(id)
    }
}
