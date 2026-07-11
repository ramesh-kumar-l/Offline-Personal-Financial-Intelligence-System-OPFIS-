package com.opfis.domain.account

import kotlinx.coroutines.flow.Flow

/**
 * Domain-owned port for account CRUD. Balance mutations from posting or
 * reversing transactions go through
 * `com.opfis.domain.transaction.FinancialLedgerPort` instead, so a
 * caller can never desync [Account.balanceMinorUnits] from the ledger.
 */
interface AccountRepository {
    fun observeAll(): Flow<List<Account>>

    fun observeById(id: String): Flow<Account?>

    suspend fun upsert(account: Account)

    suspend fun delete(id: String)
}
