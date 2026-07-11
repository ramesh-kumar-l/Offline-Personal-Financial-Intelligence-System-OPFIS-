package com.opfis.domain.search

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountType
import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryType
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FinancialSearchEngineTest {
    private val account =
        Account(
            id = "acc-1",
            name = "Primary Checking",
            type = AccountType.CHECKING,
            balanceMinorUnits = 0L,
            createdAt = 0L,
            updatedAt = 0L,
        )
    private val category =
        Category(id = "cat-1", name = "Groceries", type = CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L)
    private val transaction =
        Transaction(
            id = "tx-1",
            accountId = "acc-1",
            type = TransactionType.EXPENSE,
            amountMinorUnits = 100L,
            description = "Weekly grocery run",
            occurredAt = 0L,
            createdAt = 0L,
            updatedAt = 0L,
        )

    @Test
    fun `blank query returns no results`() {
        val results = FinancialSearchEngine.search("   ", listOf(account), listOf(category), listOf(transaction))

        assertTrue(results.isEmpty())
    }

    @Test
    fun `matches are case-insensitive across all record types`() {
        val results = FinancialSearchEngine.search("checking", listOf(account), emptyList(), emptyList())

        assertEquals(listOf(SearchResult.AccountMatch(account)), results)
    }

    @Test
    fun `query matches categories by name`() {
        val results = FinancialSearchEngine.search("grocer", emptyList(), listOf(category), emptyList())

        assertEquals(listOf(SearchResult.CategoryMatch(category)), results)
    }

    @Test
    fun `query matches transactions by description`() {
        val results = FinancialSearchEngine.search("weekly", emptyList(), emptyList(), listOf(transaction))

        assertEquals(listOf(SearchResult.TransactionMatch(transaction)), results)
    }

    @Test
    fun `query can match across multiple record types at once`() {
        val results = FinancialSearchEngine.search("gro", listOf(account), listOf(category), listOf(transaction))

        assertEquals(2, results.size)
        assertTrue(results.contains(SearchResult.CategoryMatch(category)))
        assertTrue(results.contains(SearchResult.TransactionMatch(transaction)))
    }

    @Test
    fun `no match returns an empty list`() {
        val results =
            FinancialSearchEngine.search(
                "nonexistent",
                listOf(account),
                listOf(category),
                listOf(transaction),
            )

        assertTrue(results.isEmpty())
    }
}
