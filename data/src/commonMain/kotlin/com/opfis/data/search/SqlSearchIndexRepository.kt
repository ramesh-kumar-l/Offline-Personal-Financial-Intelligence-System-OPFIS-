package com.opfis.data.search

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.account.toDomainAccount
import com.opfis.data.category.toDomainCategory
import com.opfis.data.db.OpfisDatabase
import com.opfis.data.document.toDomainDocument
import com.opfis.data.tag.toDomainTag
import com.opfis.data.transaction.toDomainTransaction
import com.opfis.domain.search.SearchEntityType
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.SearchPort
import com.opfis.domain.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * [SearchPort] backed by the `search_index` SQLite FTS5 virtual table
 * (ROADMAP Phase 4). Each entity type is its own typed join query so
 * SQLDelight's reactive `Flow` still invalidates on writes to
 * `account`/`category`/`financial_transaction`/`tag` directly, even
 * though `search_index` itself is only kept in sync by triggers.
 */
class SqlSearchIndexRepository(
    private val database: OpfisDatabase,
) : SearchPort {
    override fun search(
        query: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> {
        val matchExpression = FtsQueryBuilder.toMatchExpression(query) ?: return flowOf(emptyList())

        return combine(
            searchAccounts(matchExpression, filter),
            searchCategories(matchExpression, filter),
            searchTransactions(matchExpression, filter),
            searchTags(matchExpression, filter),
            searchDocuments(matchExpression, filter),
        ) { accounts, categories, transactions, tags, documents ->
            accounts + categories + transactions + tags + documents
        }
    }

    private fun searchAccounts(
        matchExpression: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> =
        if (SearchEntityType.ACCOUNT !in filter.entityTypes) {
            flowOf(emptyList())
        } else {
            database.searchIndexQueries
                .searchAccounts(matchExpression)
                .asFlow()
                .mapToList(Dispatchers.Default)
                .map { rows -> rows.map { SearchResult.AccountMatch(toDomainAccount(it)) } }
        }

    private fun searchCategories(
        matchExpression: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> =
        if (SearchEntityType.CATEGORY !in filter.entityTypes) {
            flowOf(emptyList())
        } else {
            database.searchIndexQueries
                .searchCategories(matchExpression)
                .asFlow()
                .mapToList(Dispatchers.Default)
                .map { rows -> rows.map { SearchResult.CategoryMatch(toDomainCategory(it)) } }
        }

    private fun searchTransactions(
        matchExpression: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> =
        if (SearchEntityType.TRANSACTION !in filter.entityTypes) {
            flowOf(emptyList())
        } else {
            database.searchIndexQueries
                .searchTransactions(matchExpression)
                .asFlow()
                .mapToList(Dispatchers.Default)
                .map { rows -> rows.map { SearchResult.TransactionMatch(toDomainTransaction(it)) } }
        }

    private fun searchTags(
        matchExpression: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> =
        if (SearchEntityType.TAG !in filter.entityTypes) {
            flowOf(emptyList())
        } else {
            database.searchIndexQueries
                .searchTags(matchExpression)
                .asFlow()
                .mapToList(Dispatchers.Default)
                .map { rows -> rows.map { SearchResult.TagMatch(toDomainTag(it)) } }
        }

    private fun searchDocuments(
        matchExpression: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>> =
        if (SearchEntityType.DOCUMENT !in filter.entityTypes) {
            flowOf(emptyList())
        } else {
            database.searchIndexQueries
                .searchDocuments(matchExpression)
                .asFlow()
                .mapToList(Dispatchers.Default)
                .map { rows -> rows.map { SearchResult.DocumentMatch(toDomainDocument(it)) } }
        }
}
