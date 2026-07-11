package com.opfis.domain.search.usecase

import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.SearchPort
import com.opfis.domain.search.SearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Application-layer use case behind global search (ROADMAP Phase 4).
 * Recomputes matches whenever the query or the filter changes, backed
 * by [SearchPort] (SQLite FTS5 in `:data`). The dashboard's search bar
 * (ROADMAP Phase 3) calls this with only a query, relying on the
 * default unfiltered [SearchFilter.All].
 */
class SearchFinancialRecordsUseCase(
    private val searchPort: SearchPort,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        query: Flow<String>,
        filter: Flow<SearchFilter> = flowOf(SearchFilter.All),
    ): Flow<List<SearchResult>> =
        query
            .combine(filter) { currentQuery, currentFilter -> currentQuery to currentFilter }
            .flatMapLatest { (currentQuery, currentFilter) -> searchPort.search(currentQuery, currentFilter) }
}
