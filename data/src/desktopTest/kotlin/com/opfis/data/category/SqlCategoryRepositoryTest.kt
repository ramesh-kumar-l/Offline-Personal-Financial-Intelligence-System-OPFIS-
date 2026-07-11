package com.opfis.data.category

import com.opfis.data.testDatabase
import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlCategoryRepositoryTest {
    @Test
    fun `upsert persists a nested category`() =
        runTest {
            val repository = SqlCategoryRepository(testDatabase())
            val parent =
                Category(id = "cat-food", name = "Food", type = CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L)
            val child =
                Category(
                    id = "cat-groceries",
                    name = "Groceries",
                    type = CategoryType.EXPENSE,
                    parentId = "cat-food",
                    createdAt = 0L,
                    updatedAt = 0L,
                )

            repository.upsert(parent)
            repository.upsert(child)
            val categories = repository.observeAll().first()

            assertEquals(2, categories.size)
            assertEquals("cat-food", categories.single { it.id == "cat-groceries" }.parentId)
        }

    @Test
    fun `delete removes the category`() =
        runTest {
            val repository = SqlCategoryRepository(testDatabase())
            repository.upsert(
                Category(id = "cat-1", name = "Food", type = CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L),
            )

            repository.delete("cat-1")

            assertEquals(emptyList(), repository.observeAll().first())
        }
}
