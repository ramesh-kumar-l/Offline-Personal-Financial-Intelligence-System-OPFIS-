package com.opfis.domain.ai.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountType
import com.opfis.domain.entity.EntityType
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.SearchPort
import com.opfis.domain.search.SearchResult
import com.opfis.domain.tag.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeSearchPort(
    private val results: List<SearchResult>,
) : SearchPort {
    override fun search(
        query: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> = flowOf(results)
}

class RetrieveFinancialContextUseCaseTest {
    @Test
    fun `maps search results to retrieved items and applies the limit`() =
        runTest {
            val account = Account("acc-1", "Checking", AccountType.CHECKING, 0L, createdAt = 0L, updatedAt = 0L)
            val tag = Tag("tag-1", "Groceries", createdAt = 0L, updatedAt = 0L)
            val useCase =
                RetrieveFinancialContextUseCase(
                    FakeSearchPort(listOf(SearchResult.AccountMatch(account), SearchResult.TagMatch(tag))),
                )

            val items = useCase("groceries", limit = 1)

            assertEquals(1, items.size)
            assertEquals(EntityType.ACCOUNT, items.first().entityType)
            assertEquals("acc-1", items.first().entityId)
        }

    @Test
    fun `default limit returns every result when fewer than the limit`() =
        runTest {
            val tag = Tag("tag-1", "Groceries", createdAt = 0L, updatedAt = 0L)
            val useCase = RetrieveFinancialContextUseCase(FakeSearchPort(listOf(SearchResult.TagMatch(tag))))

            val items = useCase("groceries")

            assertEquals(listOf(EntityType.TAG), items.map { it.entityType })
        }

    @Test
    fun `returns an empty list when nothing matches`() =
        runTest {
            val useCase = RetrieveFinancialContextUseCase(FakeSearchPort(emptyList()))
            assertEquals(emptyList(), useCase("nothing"))
        }
}
