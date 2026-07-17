package com.opfis.domain.budget.usecase

import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetPeriod
import com.opfis.domain.budget.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeBudgetRepository(
    private val budgets: List<Budget> = emptyList(),
) : BudgetRepository {
    val upserted = mutableListOf<Budget>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<Budget>> = flowOf(budgets)

    override suspend fun upsert(budget: Budget) {
        upserted.add(budget)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

class BudgetUseCasesTest {
    private val budget = Budget("budget-1", "cat-1", 20_000L, BudgetPeriod.MONTHLY, 0L, createdAt = 0L, updatedAt = 0L)

    @Test
    fun `observe budgets returns the repository stream`() =
        runTest {
            val useCase = ObserveBudgetsUseCase(FakeBudgetRepository(listOf(budget)))
            assertEquals(listOf(budget), useCase().first())
        }

    @Test
    fun `upsert budget delegates to the repository`() =
        runTest {
            val repository = FakeBudgetRepository()
            UpsertBudgetUseCase(repository)(budget)
            assertEquals(listOf(budget), repository.upserted)
        }

    @Test
    fun `delete budget delegates to the repository`() =
        runTest {
            val repository = FakeBudgetRepository()
            DeleteBudgetUseCase(repository)(budget.id)
            assertEquals(listOf(budget.id), repository.deleted)
        }
}
