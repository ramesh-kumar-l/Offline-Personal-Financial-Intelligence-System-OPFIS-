package com.opfis.domain.transaction.usecase

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveAccountTransactionsUseCase(
    private val repository: TransactionRepository,
) {
    operator fun invoke(accountId: String): Flow<List<Transaction>> = repository.observeByAccount(accountId)
}
