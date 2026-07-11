package com.opfis.domain.transaction.usecase

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Application-layer use case powering the dashboard's "Recent Activity"
 * widget (SystemPrompt Part 3, "Home Dashboard"): the [limit] most
 * recently occurred transactions across all accounts.
 */
class ObserveRecentTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(limit: Int = DEFAULT_LIMIT): Flow<List<Transaction>> =
        transactionRepository.observeAll().map { transactions ->
            transactions.sortedByDescending { it.occurredAt }.take(limit)
        }

    companion object {
        const val DEFAULT_LIMIT = 10
    }
}
