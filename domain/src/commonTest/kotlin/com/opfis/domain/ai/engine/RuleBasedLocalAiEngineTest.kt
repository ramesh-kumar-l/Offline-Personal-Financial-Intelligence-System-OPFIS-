package com.opfis.domain.ai.engine

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountRepository
import com.opfis.domain.account.AccountType
import com.opfis.domain.ai.FinancialRepositories
import com.opfis.domain.ai.usecase.BuildFinancialSnapshotUseCase
import com.opfis.domain.ai.usecase.RetrieveFinancialContextUseCase
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetPeriod
import com.opfis.domain.budget.BudgetRepository
import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.category.CategoryType
import com.opfis.domain.entity.EntityType
import com.opfis.domain.goal.Goal
import com.opfis.domain.goal.GoalRepository
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.SearchPort
import com.opfis.domain.search.SearchResult
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionRepository
import com.opfis.domain.transaction.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeAccountRepository(
    private val accounts: List<Account> = emptyList(),
) : AccountRepository {
    override fun observeAll(): Flow<List<Account>> = flowOf(accounts)

    override fun observeById(id: String): Flow<Account?> = flowOf(accounts.find { it.id == id })

    override suspend fun upsert(account: Account) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

private class FakeAssetRepository : AssetRepository {
    override fun observeAll() = flowOf(emptyList<com.opfis.domain.asset.Asset>())

    override suspend fun upsert(asset: com.opfis.domain.asset.Asset) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

private class FakeLiabilityRepository : LiabilityRepository {
    override fun observeAll() = flowOf(emptyList<com.opfis.domain.liability.Liability>())

    override suspend fun upsert(liability: com.opfis.domain.liability.Liability) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

private class FakeTransactionRepository(
    private val transactions: List<Transaction> = emptyList(),
) : TransactionRepository {
    override fun observeAll(): Flow<List<Transaction>> = flowOf(transactions)

    override fun observeByAccount(accountId: String) = flowOf(transactions.filter { it.accountId == accountId })

    override fun observeRecent(limit: Int): Flow<List<Transaction>> = error("not used in this test")
}

private class FakeCategoryRepository(
    private val categories: List<Category> = emptyList(),
) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> = flowOf(categories)

    override suspend fun upsert(category: Category) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

private class FakeBudgetRepository(
    private val budgets: List<Budget> = emptyList(),
) : BudgetRepository {
    override fun observeAll(): Flow<List<Budget>> = flowOf(budgets)

    override suspend fun upsert(budget: Budget) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

private class FakeGoalRepository(
    private val goals: List<Goal> = emptyList(),
) : GoalRepository {
    override fun observeAll(): Flow<List<Goal>> = flowOf(goals)

    override suspend fun upsert(goal: Goal) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

private class FakeSearchPort : SearchPort {
    override fun search(
        query: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> = flowOf(emptyList())
}

private fun engineWith(
    accounts: List<Account> = emptyList(),
    transactions: List<Transaction> = emptyList(),
    categories: List<Category> = emptyList(),
    budgets: List<Budget> = emptyList(),
    goals: List<Goal> = emptyList(),
): RuleBasedLocalAiEngine {
    val repositories =
        FinancialRepositories(
            accounts = FakeAccountRepository(accounts),
            assets = FakeAssetRepository(),
            liabilities = FakeLiabilityRepository(),
            transactions = FakeTransactionRepository(transactions),
            categories = FakeCategoryRepository(categories),
            budgets = FakeBudgetRepository(budgets),
            goals = FakeGoalRepository(goals),
        )
    return RuleBasedLocalAiEngine(
        BuildFinancialSnapshotUseCase(repositories),
        RetrieveFinancialContextUseCase(FakeSearchPort()),
    )
}

class RuleBasedLocalAiEngineTest {
    @Test
    fun `net worth question cites the accounts it summed`() =
        runTest {
            val account = Account("acc-1", "Checking", AccountType.CHECKING, 10_000L, createdAt = 0L, updatedAt = 0L)
            val answer = engineWith(accounts = listOf(account)).answer("What is my net worth?", asOfEpochMillis = 0L)

            assertTrue(answer.text.contains("100.00"))
            assertEquals(listOf(EntityType.ACCOUNT), answer.citations.map { it.entityType })
        }

    @Test
    fun `spending question narrows to the mentioned category`() =
        runTest {
            val groceries = Category("cat-1", "Groceries", CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L)
            val other = Category("cat-2", "Rent", CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L)
            val groceryTx =
                Transaction(
                    "tx-1",
                    "acc-1",
                    "cat-1",
                    TransactionType.EXPENSE,
                    5_000L,
                    occurredAt = 0L,
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            val rentTx =
                Transaction(
                    "tx-2",
                    "acc-1",
                    "cat-2",
                    TransactionType.EXPENSE,
                    9_000L,
                    occurredAt = 0L,
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            val answer =
                engineWith(transactions = listOf(groceryTx, rentTx), categories = listOf(groceries, other))
                    .answer("How much did I spend on Groceries?", asOfEpochMillis = 0L)

            assertTrue(answer.text.contains("50.00"))
            assertEquals(listOf("tx-1"), answer.citations.map { it.entityId })
        }

    @Test
    fun `budget question lists limits without claiming spend-to-date status`() =
        runTest {
            val category = Category("cat-1", "Groceries", CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L)
            val budget = Budget("budget-1", "cat-1", 20_000L, BudgetPeriod.MONTHLY, 0L, createdAt = 0L, updatedAt = 0L)
            val answer =
                engineWith(categories = listOf(category), budgets = listOf(budget))
                    .answer("What is my Groceries budget?", asOfEpochMillis = 0L)

            assertTrue(answer.text.contains("200.00"))
            assertTrue(answer.text.contains("not yet implemented"))
        }

    @Test
    fun `goal question reports progress toward the target`() =
        runTest {
            val goal = Goal("goal-1", "Emergency Fund", 100_000L, 25_000L, createdAt = 0L, updatedAt = 0L)
            val answer = engineWith(goals = listOf(goal)).answer("How is my Emergency Fund goal?", asOfEpochMillis = 0L)

            assertTrue(answer.text.contains("250.00"))
            assertTrue(answer.text.contains("1,000.00"))
        }

    @Test
    fun `general question with no matches says so explicitly`() =
        runTest {
            val answer = engineWith().answer("Show me my receipts from Paris", asOfEpochMillis = 0L)

            assertTrue(answer.text.contains("could not find"))
            assertEquals(emptyList(), answer.citations)
        }
}
