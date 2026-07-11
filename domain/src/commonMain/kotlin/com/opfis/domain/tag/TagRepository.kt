package com.opfis.domain.tag

import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeAll(): Flow<List<Tag>>

    suspend fun upsert(tag: Tag)

    suspend fun delete(id: String)
}
