package com.opfis.data.budget

import com.opfis.data.testDatabase
import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetPeriod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlBudgetRepositoryTest {
    @Test
    fun `upsert persists a monthly budget limit`() =
        runTest {
            val repository = SqlBudgetRepository(testDatabase())
            val budget =
                Budget(
                    id = "budget-1",
                    categoryId = "cat-food",
                    limitMinorUnits = 20_000L,
                    period = BudgetPeriod.MONTHLY,
                    startDate = 1000L,
                    createdAt = 0L,
                    updatedAt = 0L,
                )

            repository.upsert(budget)
            val budgets = repository.observeAll().first()

            assertEquals(BudgetPeriod.MONTHLY, budgets.single().period)
            assertEquals(20_000L, budgets.single().limitMinorUnits)
        }
}
