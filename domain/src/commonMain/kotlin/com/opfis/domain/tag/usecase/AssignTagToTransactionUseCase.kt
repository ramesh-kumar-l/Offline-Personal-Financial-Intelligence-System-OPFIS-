package com.opfis.domain.tag.usecase

import com.opfis.domain.tag.TransactionTagRepository

class AssignTagToTransactionUseCase(
    private val repository: TransactionTagRepository,
) {
    suspend operator fun invoke(
        transactionId: String,
        tagId: String,
    ) = repository.assignTag(transactionId, tagId)
}
