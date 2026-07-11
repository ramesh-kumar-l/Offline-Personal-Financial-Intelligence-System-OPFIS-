package com.opfis.domain.search

/**
 * Narrows a [SearchPort] query or a timeline browse to a subset of
 * records (ROADMAP Phase 4, "Filters"). [tagIds] and the occurred-at
 * range only apply to transactions - accounts/categories/tags have no
 * date or tag of their own.
 */
data class SearchFilter(
    val entityTypes: Set<SearchEntityType> = SearchEntityType.entries.toSet(),
    val tagIds: Set<String> = emptySet(),
    val occurredFrom: Long? = null,
    val occurredTo: Long? = null,
) {
    companion object {
        val All = SearchFilter()
    }
}

enum class SearchEntityType {
    ACCOUNT,
    CATEGORY,
    TRANSACTION,
    TAG,
}
