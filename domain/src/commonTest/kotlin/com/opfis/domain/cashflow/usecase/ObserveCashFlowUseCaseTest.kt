package com.opfis.domain.cashflow.usecase

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionRepository
import com.opfis.domain.transaction.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTransactionRepository(
    private val transactions: List<Transaction>,
) : TransactionRepository {
    override fun observeAll(): Flow<List<Transaction>> = flowOf(transactions)

    override fun observeByAccount(accountId: String): Flow<List<Transaction>> = error("not used in this test")

    override fun observeRecent(limit: Int): Flow<List<Transaction>> = error("not used in this test")
}

class ObserveCashFlowUseCaseTest {
    @Test
    fun `observe cash flow always returns exactly monthCount periods`() =
        runTest {
            val transaction =
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
            val useCase = ObserveCashFlowUseCase(FakeTransactionRepository(listOf(transaction)))

            val periods = useCase(monthCount = 3).first()

            assertEquals(3, periods.size)
        }
}
