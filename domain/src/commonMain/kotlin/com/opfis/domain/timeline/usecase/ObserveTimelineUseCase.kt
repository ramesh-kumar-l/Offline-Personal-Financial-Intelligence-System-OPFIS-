package com.opfis.domain.timeline.usecase

import com.opfis.domain.search.SearchEntityType
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.tag.TransactionTagRepository
import com.opfis.domain.timeline.TimelineEntry
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Chronological, filterable transaction browse (ROADMAP Phase 4,
 * "Timeline search" + "Filters"). Applies [SearchFilter]'s entity-type
 * toggle, tag membership, and occurred-at range, most recent first.
 */
class ObserveTimelineUseCase(
    private val transactionRepository: TransactionRepository,
    private val transactionTagRepository: TransactionTagRepository,
) {
    operator fun invoke(filter: Flow<SearchFilter>): Flow<List<TimelineEntry>> =
        combine(
            transactionRepository.observeAll(),
            transactionTagRepository.observeTagIdsByTransaction(),
            filter,
        ) { transactions, tagsByTransaction, currentFilter ->
            if (SearchEntityType.TRANSACTION !in currentFilter.entityTypes) {
                return@combine emptyList()
            }
            transactions
                .asSequence()
                .filter { currentFilter.occurredFrom == null || it.occurredAt >= currentFilter.occurredFrom }
                .filter { currentFilter.occurredTo == null || it.occurredAt <= currentFilter.occurredTo }
                .filter { transaction ->
                    currentFilter.tagIds.isEmpty() ||
                        tagsByTransaction[transaction.id].orEmpty().any { it in currentFilter.tagIds }
                }.sortedByDescending { it.occurredAt }
                .map { transaction -> TimelineEntry(transaction, tagsByTransaction[transaction.id].orEmpty()) }
                .toList()
        }
}
