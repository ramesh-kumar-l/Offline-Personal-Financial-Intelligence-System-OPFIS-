package com.opfis.domain.tag.usecase

import com.opfis.domain.tag.TransactionTagRepository

class RemoveTagFromTransactionUseCase(
    private val repository: TransactionTagRepository,
) {
    suspend operator fun invoke(
        transactionId: String,
        tagId: String,
    ) = repository.unassignTag(transactionId, tagId)
}
