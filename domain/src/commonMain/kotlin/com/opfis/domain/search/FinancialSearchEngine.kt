package com.opfis.domain.search

import com.opfis.domain.account.Account
import com.opfis.domain.category.Category
import com.opfis.domain.transaction.Transaction

/**
 * Pure, in-memory, case-insensitive substring search across the
 * financial record set. Deliberately simple: this is the dashboard's
 * search entry point (ROADMAP Phase 3). SQLite FTS5, ranked global
 * search, filters, timeline search, and tags are Phase 4 scope - see
 * `14-search-engine.md`.
 */
object FinancialSearchEngine {
    fun search(
        query: String,
        accounts: List<Account>,
        categories: List<Category>,
        transactions: List<Transaction>,
    ): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val accountMatches =
            accounts
                .filter { it.name.contains(trimmed, ignoreCase = true) }
                .map(SearchResult::AccountMatch)
        val categoryMatches =
            categories
                .filter { it.name.contains(trimmed, ignoreCase = true) }
                .map(SearchResult::CategoryMatch)
        val transactionMatches =
            transactions
                .filter { it.description.contains(trimmed, ignoreCase = true) }
                .map(SearchResult::TransactionMatch)

        return accountMatches + categoryMatches + transactionMatches
    }
}
