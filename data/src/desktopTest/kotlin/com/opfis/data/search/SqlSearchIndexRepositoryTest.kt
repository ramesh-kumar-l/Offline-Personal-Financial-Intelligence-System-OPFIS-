package com.opfis.data.search

import com.opfis.data.testDatabase
import com.opfis.domain.search.SearchEntityType
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.SearchResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlSearchIndexRepositoryTest {
    @Test
    fun `finds an account by a case-insensitive name prefix`() =
        runTest {
            val database = testDatabase()
            database.accountQueries.insertOrReplace(
                id = "acc-1",
                name = "Primary Checking",
                type = "CHECKING",
                balance_minor_units = 0L,
                is_archived = 0L,
                created_at = 0L,
                updated_at = 0L,
                version = 1L,
            )

            val results = SqlSearchIndexRepository(database).search("checking", SearchFilter.All).first()

            assertEquals(1, results.size)
            assertTrue(results.single() is SearchResult.AccountMatch)
        }

    @Test
    fun `finds a transaction by a description token`() =
        runTest {
            val database = testDatabase()
            database.financialTransactionQueries.insertOrReplace(
                id = "tx-1",
                account_id = "acc-1",
                category_id = null,
                type = "EXPENSE",
                amount_minor_units = 500L,
                transfer_account_id = null,
                description = "Weekly grocery run",
                occurred_at = 0L,
                created_at = 0L,
                updated_at = 0L,
                version = 1L,
            )

            val results = SqlSearchIndexRepository(database).search("grocery", SearchFilter.All).first()

            assertEquals(1, results.size)
            assertTrue(results.single() is SearchResult.TransactionMatch)
        }

    @Test
    fun `blank query returns no results`() =
        runTest {
            val results = SqlSearchIndexRepository(testDatabase()).search("   ", SearchFilter.All).first()

            assertEquals(emptyList(), results)
        }

    @Test
    fun `entity type filter excludes disabled types`() =
        runTest {
            val database = testDatabase()
            database.accountQueries.insertOrReplace(
                id = "acc-1",
                name = "Grocery Fund",
                type = "SAVINGS",
                balance_minor_units = 0L,
                is_archived = 0L,
                created_at = 0L,
                updated_at = 0L,
                version = 1L,
            )
            val filter = SearchFilter(entityTypes = setOf(SearchEntityType.TRANSACTION))

            val results = SqlSearchIndexRepository(database).search("grocery", filter).first()

            assertEquals(emptyList(), results)
        }

    @Test
    fun `deleting the source row removes it from search results`() =
        runTest {
            val database = testDatabase()
            database.categoryQueries.insertOrReplace(
                id = "cat-1",
                name = "Groceries",
                type = "EXPENSE",
                parent_id = null,
                created_at = 0L,
                updated_at = 0L,
                version = 1L,
            )
            val repository = SqlSearchIndexRepository(database)
            assertEquals(1, repository.search("groceries", SearchFilter.All).first().size)

            database.categoryQueries.deleteById("cat-1")

            assertEquals(emptyList(), repository.search("groceries", SearchFilter.All).first())
        }
}
