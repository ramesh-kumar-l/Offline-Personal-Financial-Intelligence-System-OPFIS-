package com.opfis.domain.search

import com.opfis.domain.account.Account
import com.opfis.domain.category.Category
import com.opfis.domain.transaction.Transaction

/**
 * A single match surfaced by [FinancialSearchEngine]. This is the
 * dashboard's search entry point (ROADMAP Phase 3); global full-text
 * search with filters/tags/timeline is Phase 4 ("Search") scope.
 */
sealed class SearchResult {
    data class AccountMatch(
        val account: Account,
    ) : SearchResult()

    data class CategoryMatch(
        val category: Category,
    ) : SearchResult()

    data class TransactionMatch(
        val transaction: Transaction,
    ) : SearchResult()
}
