package com.opfis.data.tag

import com.opfis.data.testDatabase
import com.opfis.domain.tag.Tag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlTagRepositoryTest {
    @Test
    fun `upsert persists a tag`() =
        runTest {
            val repository = SqlTagRepository(testDatabase())
            repository.upsert(Tag(id = "tag-1", name = "Groceries", createdAt = 0L, updatedAt = 0L))

            val tags = repository.observeAll().first()

            assertEquals("Groceries", tags.single().name)
        }

    @Test
    fun `delete removes the tag`() =
        runTest {
            val repository = SqlTagRepository(testDatabase())
            repository.upsert(Tag(id = "tag-1", name = "Groceries", createdAt = 0L, updatedAt = 0L))

            repository.delete("tag-1")

            assertEquals(emptyList(), repository.observeAll().first())
        }
}
