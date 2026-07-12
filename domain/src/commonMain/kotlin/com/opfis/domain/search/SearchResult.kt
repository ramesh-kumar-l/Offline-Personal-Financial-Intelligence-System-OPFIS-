package com.opfis.domain.search

import com.opfis.domain.account.Account
import com.opfis.domain.category.Category
import com.opfis.domain.document.Document
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.tag.Tag
import com.opfis.domain.transaction.Transaction

/**
 * A single match surfaced by [SearchPort] (ROADMAP Phase 4: global
 * search across accounts, categories, transactions, and tags; Phase 5
 * adds documents; Phase 6 adds memory events).
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

    data class DocumentMatch(
        val document: Document,
    ) : SearchResult()

    data class MemoryEventMatch(
        val memoryEvent: MemoryEvent,
    ) : SearchResult()
}
