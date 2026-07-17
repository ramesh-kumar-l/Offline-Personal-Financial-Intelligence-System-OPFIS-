package com.opfis.domain.ai.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountRepository
import com.opfis.domain.account.AccountType
import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.FinancialRepositories
import com.opfis.domain.ai.LocalAiPort
import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.asset.AssetType
import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetPeriod
import com.opfis.domain.budget.BudgetRepository
import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.category.CategoryType
import com.opfis.domain.goal.Goal
import com.opfis.domain.goal.GoalRepository
import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.liability.LiabilityType
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionRepository
import com.opfis.domain.transaction.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeLocalAiPort(
    private val answer: AiAnswer,
) : LocalAiPort {
    var lastQuestion: String? = null

    override suspend fun answer(
        question: String,
        asOfEpochMillis: Long,
    ): AiAnswer {
        lastQuestion = question
        return answer
    }
}

private class SingleItemFakeRepositories(
    val account: Account,
    val asset: Asset,
    val liability: Liability,
    val transaction: Transaction,
    val category: Category,
    val budget: Budget,
    val goal: Goal,
) {
    fun toFinancialRepositories() =
        FinancialRepositories(
            accounts =
                object : AccountRepository {
                    override fun observeAll(): Flow<List<Account>> = flowOf(listOf(account))

                    override fun observeById(id: String): Flow<Account?> = error("not used in this test")

                    override suspend fun upsert(account: Account) = error("not used in this test")

                    override suspend fun delete(id: String) = error("not used in this test")
                },
            assets =
                object : AssetRepository {
                    override fun observeAll(): Flow<List<Asset>> = flowOf(listOf(asset))

                    override suspend fun upsert(asset: Asset) = error("not used in this test")

                    override suspend fun delete(id: String) = error("not used in this test")
                },
            liabilities =
                object : LiabilityRepository {
                    override fun observeAll(): Flow<List<Liability>> = flowOf(listOf(liability))

                    override suspend fun upsert(liability: Liability) = error("not used in this test")

                    override suspend fun delete(id: String) = error("not used in this test")
                },
            transactions =
                object : TransactionRepository {
                    override fun observeAll(): Flow<List<Transaction>> = flowOf(listOf(transaction))

                    override fun observeByAccount(accountId: String): Flow<List<Transaction>> =
                        error("not used in this test")

                    override fun observeRecent(limit: Int): Flow<List<Transaction>> = error("not used in this test")
                },
            categories =
                object : CategoryRepository {
                    override fun observeAll(): Flow<List<Category>> = flowOf(listOf(category))

                    override suspend fun upsert(category: Category) = error("not used in this test")

                    override suspend fun delete(id: String) = error("not used in this test")
                },
            budgets =
                object : BudgetRepository {
                    override fun observeAll(): Flow<List<Budget>> = flowOf(listOf(budget))

                    override suspend fun upsert(budget: Budget) = error("not used in this test")

                    override suspend fun delete(id: String) = error("not used in this test")
                },
            goals =
                object : GoalRepository {
                    override fun observeAll(): Flow<List<Goal>> = flowOf(listOf(goal))

                    override suspend fun upsert(goal: Goal) = error("not used in this test")

                    override suspend fun delete(id: String) = error("not used in this test")
                },
        )
}

class AiUseCasesTest {
    @Test
    fun `ask ai assistant delegates the question to the local ai port`() =
        runTest {
            val answer = AiAnswer("Your net worth is 100.")
            val port = FakeLocalAiPort(answer)

            val result = AskAiAssistantUseCase(port)("What is my net worth?", asOfEpochMillis = 0L)

            assertEquals(answer, result)
            assertEquals("What is my net worth?", port.lastQuestion)
        }

    @Test
    fun `build financial snapshot assembles one item from every repository`() =
        runTest {
            val fakes =
                SingleItemFakeRepositories(
                    account = Account("acc-1", "Checking", AccountType.CHECKING, 0L, createdAt = 0L, updatedAt = 0L),
                    asset = Asset("asset-1", "House", AssetType.REAL_ESTATE, 0L, createdAt = 0L, updatedAt = 0L),
                    liability = Liability("liab-1", "Car Loan", LiabilityType.LOAN, 0L, createdAt = 0L, updatedAt = 0L),
                    transaction =
                        Transaction(
                            "tx-1",
                            "acc-1",
                            "cat-1",
                            TransactionType.EXPENSE,
                            100L,
                            occurredAt = 0L,
                            createdAt = 0L,
                            updatedAt = 0L,
                        ),
                    category = Category("cat-1", "Groceries", CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L),
                    budget =
                        Budget("budget-1", "cat-1", 1_000L, BudgetPeriod.MONTHLY, 0L, createdAt = 0L, updatedAt = 0L),
                    goal = Goal("goal-1", "Emergency Fund", 1_000L, 0L, createdAt = 0L, updatedAt = 0L),
                )

            val snapshot = BuildFinancialSnapshotUseCase(fakes.toFinancialRepositories())()

            assertEquals(listOf(fakes.account), snapshot.accounts)
            assertEquals(listOf(fakes.asset), snapshot.assets)
            assertEquals(listOf(fakes.liability), snapshot.liabilities)
            assertEquals(listOf(fakes.transaction), snapshot.transactions)
            assertEquals(listOf(fakes.category), snapshot.categories)
            assertEquals(listOf(fakes.budget), snapshot.budgets)
            assertEquals(listOf(fakes.goal), snapshot.goals)
        }
}
