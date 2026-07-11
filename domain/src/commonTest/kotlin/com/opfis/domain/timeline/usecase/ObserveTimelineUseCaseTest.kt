package com.opfis.domain.timeline.usecase

import com.opfis.domain.search.SearchEntityType
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.tag.TransactionTagRepository
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
}

private class FakeTransactionTagRepository(
    private val tagsByTransaction: Map<String, List<String>>,
) : TransactionTagRepository {
    override fun observeTagIdsByTransaction(): Flow<Map<String, List<String>>> = flowOf(tagsByTransaction)

    override suspend fun assignTag(
        transactionId: String,
        tagId: String,
    ) = error("not used in this test")

    override suspend fun unassignTag(
        transactionId: String,
        tagId: String,
    ) = error("not used in this test")
}

class ObserveTimelineUseCaseTest {
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
    fun `entries are sorted most recent first`() =
        runTest {
            val useCase =
                ObserveTimelineUseCase(
                    FakeTransactionRepository(listOf(transaction("tx-1", 1_000L), transaction("tx-2", 2_000L))),
                    FakeTransactionTagRepository(emptyMap()),
                )

            val entries = useCase(flowOf(SearchFilter.All)).first()

            assertEquals(listOf("tx-2", "tx-1"), entries.map { it.transaction.id })
        }

    @Test
    fun `excludes transactions outside the occurred-at range`() =
        runTest {
            val useCase =
                ObserveTimelineUseCase(
                    FakeTransactionRepository(listOf(transaction("tx-1", 1_000L), transaction("tx-2", 5_000L))),
                    FakeTransactionTagRepository(emptyMap()),
                )

            val entries = useCase(flowOf(SearchFilter(occurredFrom = 2_000L))).first()

            assertEquals(listOf("tx-2"), entries.map { it.transaction.id })
        }

    @Test
    fun `filters by tag membership`() =
        runTest {
            val useCase =
                ObserveTimelineUseCase(
                    FakeTransactionRepository(listOf(transaction("tx-1", 1_000L), transaction("tx-2", 2_000L))),
                    FakeTransactionTagRepository(mapOf("tx-1" to listOf("tag-groceries"))),
                )

            val entries = useCase(flowOf(SearchFilter(tagIds = setOf("tag-groceries")))).first()

            assertEquals(listOf("tx-1"), entries.map { it.transaction.id })
            assertEquals(listOf("tag-groceries"), entries.single().tagIds)
        }

    @Test
    fun `excluding TRANSACTION from entity types yields an empty timeline`() =
        runTest {
            val useCase =
                ObserveTimelineUseCase(
                    FakeTransactionRepository(listOf(transaction("tx-1", 1_000L))),
                    FakeTransactionTagRepository(emptyMap()),
                )

            val entries = useCase(flowOf(SearchFilter(entityTypes = setOf(SearchEntityType.ACCOUNT)))).first()

            assertEquals(emptyList(), entries)
        }
}
