package com.opfis.domain.transaction.usecase

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

    override fun observeByAccount(accountId: String): Flow<List<Transaction>> = flowOf(transactions)
}

class ObserveRecentTransactionsUseCaseTest {
    private fun transaction(
        id: String,
        occurredAt: Long,
    ) = Transaction(
        id = id,
        accountId = "acc-1",
        type = TransactionType.EXPENSE,
        amountMinorUnits = 100L,
        occurredAt = occurredAt,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `invoke returns transactions newest first`() =
        runTest {
            val repository =
                FakeTransactionRepository(
                    listOf(
                        transaction("old", occurredAt = 1L),
                        transaction("new", occurredAt = 3L),
                        transaction("mid", occurredAt = 2L),
                    ),
                )
            val useCase = ObserveRecentTransactionsUseCase(repository)

            val result = useCase().first()

            assertEquals(listOf("new", "mid", "old"), result.map { it.id })
        }

    @Test
    fun `invoke respects the limit`() =
        runTest {
            val repository =
                FakeTransactionRepository((1..5).map { transaction("tx-$it", occurredAt = it.toLong()) })
            val useCase = ObserveRecentTransactionsUseCase(repository)

            val result = useCase(limit = 2).first()

            assertEquals(listOf("tx-5", "tx-4"), result.map { it.id })
        }
}
