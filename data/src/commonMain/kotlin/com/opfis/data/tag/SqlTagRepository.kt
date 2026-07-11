package com.opfis.data.tag

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.tag.Tag
import com.opfis.domain.tag.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlTagRepository(
    private val database: OpfisDatabase,
) : TagRepository {
    override fun observeAll(): Flow<List<Tag>> =
        database.tagQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainTag) }

    override suspend fun upsert(tag: Tag) {
        val existingVersion =
            database.tagQueries
                .selectById(tag.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.tagQueries.insertOrReplace(
            id = tag.id,
            name = tag.name,
            color_hex = tag.colorHex,
            created_at = tag.createdAt,
            updated_at = tag.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.tagQueries.deleteById(id)
    }
}
