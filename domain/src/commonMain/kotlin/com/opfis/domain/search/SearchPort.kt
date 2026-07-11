package com.opfis.domain.search

import kotlinx.coroutines.flow.Flow

/**
 * Write-free read port behind global search (ROADMAP Phase 4). The
 * `:data` implementation is expected to use SQLite FTS5 for ranked,
 * instant, offline matching - see `14-search-engine.md`.
 */
interface SearchPort {
    fun search(
        query: String,
        filter: SearchFilter,
    ): Flow<List<SearchResult>>
}
