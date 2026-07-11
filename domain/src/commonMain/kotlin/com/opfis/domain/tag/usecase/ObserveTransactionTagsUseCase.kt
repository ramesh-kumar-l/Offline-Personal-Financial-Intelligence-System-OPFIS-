package com.opfis.domain.tag.usecase

import com.opfis.domain.tag.TransactionTagRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionTagsUseCase(
    private val repository: TransactionTagRepository,
) {
    operator fun invoke(): Flow<Map<String, List<String>>> = repository.observeTagIdsByTransaction()
}
