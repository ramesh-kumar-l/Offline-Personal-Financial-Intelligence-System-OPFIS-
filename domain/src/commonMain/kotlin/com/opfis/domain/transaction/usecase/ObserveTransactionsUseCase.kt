package com.opfis.domain.transaction.usecase

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionsUseCase(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<Transaction>> = repository.observeAll()
}
