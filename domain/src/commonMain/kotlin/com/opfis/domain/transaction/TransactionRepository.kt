package com.opfis.domain.transaction

import kotlinx.coroutines.flow.Flow

/**
 * Read-side port for transactions. Mutations go through
 * [FinancialLedgerPort] instead, since posting or deleting a
 * transaction must also atomically adjust account balances.
 */
interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>

    fun observeByAccount(accountId: String): Flow<List<Transaction>>

    /** The [limit] most recently occurred transactions, newest first - bounded at the query level (ROADMAP Phase 10). */
    fun observeRecent(limit: Int): Flow<List<Transaction>>
}
