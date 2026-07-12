package com.opfis.domain.ai.usecase

import com.opfis.domain.ai.RetrievedItem
import com.opfis.domain.entity.EntityType
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.SearchPort
import com.opfis.domain.search.SearchResult
import kotlinx.coroutines.flow.first

/**
 * Semantic-retrieval layer for [com.opfis.domain.ai.LocalAiPort] (ROADMAP
 * Phase 7). No embedding model is available fully offline in this
 * environment, so retrieval is lexical - it reuses Phase 4's FTS5-backed
 * [SearchPort] rather than true vector similarity. Documented as a
 * deliberate scope cut, not an oversight - see `15-ai-runtime.md`.
 */
class RetrieveFinancialContextUseCase(
    private val searchPort: SearchPort,
) {
    suspend operator fun invoke(
        query: String,
        limit: Int = DEFAULT_LIMIT,
    ): List<RetrievedItem> =
        searchPort
            .search(query, SearchFilter.All)
            .first()
            .take(limit)
            .map { it.toRetrievedItem() }

    private fun SearchResult.toRetrievedItem(): RetrievedItem =
        when (this) {
            is SearchResult.AccountMatch -> RetrievedItem(EntityType.ACCOUNT, account.id, "Account: ${account.name}")
            is SearchResult.CategoryMatch ->
                RetrievedItem(
                    EntityType.CATEGORY,
                    category.id,
                    "Category: ${category.name}",
                )
            is SearchResult.TransactionMatch ->
                RetrievedItem(EntityType.TRANSACTION, transaction.id, "Transaction: ${transaction.description}")
            is SearchResult.TagMatch -> RetrievedItem(EntityType.TAG, tag.id, "Tag: ${tag.name}")
            is SearchResult.DocumentMatch ->
                RetrievedItem(
                    EntityType.DOCUMENT,
                    document.id,
                    "Document: ${document.fileName}",
                )
            is SearchResult.MemoryEventMatch ->
                RetrievedItem(EntityType.MEMORY_EVENT, memoryEvent.id, "Memory: ${memoryEvent.title}")
        }

    private companion object {
        const val DEFAULT_LIMIT = 5
    }
}
