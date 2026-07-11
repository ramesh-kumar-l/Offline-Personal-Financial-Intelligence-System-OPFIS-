package com.opfis.domain.transaction

/**
 * Write-side port for posting/reversing transactions. Implementations
 * (`:data`) must apply the transaction row write and every
 * [TransactionLedgerRules] balance delta as one atomic unit (SYSTEM_PROMPT
 * Part 2: "All writes must be Atomic, Transactional, Recoverable") so an
 * account balance can never drift from its posted transaction history.
 */
interface FinancialLedgerPort {
    suspend fun recordTransaction(transaction: Transaction)

    suspend fun deleteTransaction(transactionId: String)
}
