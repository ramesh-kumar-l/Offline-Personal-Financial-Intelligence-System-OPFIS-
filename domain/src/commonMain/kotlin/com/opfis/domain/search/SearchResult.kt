package com.opfis.domain.search

import com.opfis.domain.account.Account
import com.opfis.domain.category.Category
import com.opfis.domain.tag.Tag
import com.opfis.domain.transaction.Transaction

/**
 * A single match surfaced by [SearchPort] (ROADMAP Phase 4: global
 * search across accounts, categories, transactions, and tags).
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

    data class TagMatch(
        val tag: Tag,
    ) : SearchResult()
}
