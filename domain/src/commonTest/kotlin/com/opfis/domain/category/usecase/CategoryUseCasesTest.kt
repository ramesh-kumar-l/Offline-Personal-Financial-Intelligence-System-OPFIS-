package com.opfis.domain.category.usecase

import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.category.CategoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeCategoryRepository(
    private val categories: List<Category> = emptyList(),
) : CategoryRepository {
    val upserted = mutableListOf<Category>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<Category>> = flowOf(categories)

    override suspend fun upsert(category: Category) {
        upserted.add(category)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

class CategoryUseCasesTest {
    private val category = Category("cat-1", "Groceries", CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L)

    @Test
    fun `observe categories returns the repository stream`() =
        runTest {
            val useCase = ObserveCategoriesUseCase(FakeCategoryRepository(listOf(category)))
            assertEquals(listOf(category), useCase().first())
        }

    @Test
    fun `upsert category delegates to the repository`() =
        runTest {
            val repository = FakeCategoryRepository()
            UpsertCategoryUseCase(repository)(category)
            assertEquals(listOf(category), repository.upserted)
        }

    @Test
    fun `delete category delegates to the repository`() =
        runTest {
            val repository = FakeCategoryRepository()
            DeleteCategoryUseCase(repository)(category.id)
            assertEquals(listOf(category.id), repository.deleted)
        }
}
