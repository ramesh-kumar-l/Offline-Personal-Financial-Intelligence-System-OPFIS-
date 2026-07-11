package com.opfis.domain.search.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountType
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.SearchPort
import com.opfis.domain.search.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeSearchPort : SearchPort {
    var lastQuery: String? = null
    var lastFilter: SearchFilter? = null

    override fun search(
        query: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> {
        lastQuery = query
        lastFilter = filter
        val account =
            Account(
                id = "acc-1",
                name = query,
                type = AccountType.CHECKING,
                balanceMinorUnits = 0L,
                createdAt = 0L,
                updatedAt = 0L,
            )
        return flowOf(listOf(SearchResult.AccountMatch(account)))
    }
}

class SearchFinancialRecordsUseCaseTest {
    @Test
    fun `defaults to an unfiltered search when no filter is supplied`() =
        runTest {
            val port = FakeSearchPort()
            val useCase = SearchFinancialRecordsUseCase(port)

            val results = useCase(flowOf("checking")).first()

            assertEquals("checking", port.lastQuery)
            assertEquals(SearchFilter.All, port.lastFilter)
            assertEquals(1, results.size)
        }

    @Test
    fun `forwards an explicit filter to the port`() =
        runTest {
            val port = FakeSearchPort()
            val useCase = SearchFinancialRecordsUseCase(port)
            val filter = SearchFilter(tagIds = setOf("tag-1"))

            useCase(flowOf("groceries"), flowOf(filter)).first()

            assertEquals(filter, port.lastFilter)
        }
}
