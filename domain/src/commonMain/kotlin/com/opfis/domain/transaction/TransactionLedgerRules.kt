package com.opfis.domain.transaction

/**
 * Domain policy translating a [Transaction] into signed balance deltas
 * per affected account. Pure and framework-free (SYSTEM_PROMPT Part 2:
 * "Domain Layer ... Policies"), so it is unit-testable without a
 * database and is the single place the INCOME/EXPENSE/TRANSFER sign
 * convention is defined.
 */
object TransactionLedgerRules {
    fun accountDeltas(transaction: Transaction): Map<String, Long> =
        when (transaction.type) {
            TransactionType.INCOME ->
                mapOf(transaction.accountId to transaction.amountMinorUnits)
            TransactionType.EXPENSE ->
                mapOf(transaction.accountId to -transaction.amountMinorUnits)
            TransactionType.TRANSFER -> {
                val destinationAccountId = requireNotNull(transaction.transferAccountId)
                mapOf(
                    transaction.accountId to -transaction.amountMinorUnits,
                    destinationAccountId to transaction.amountMinorUnits,
                )
            }
        }

    /** The inverse of [accountDeltas], applied when a transaction is deleted. */
    fun reversalDeltas(transaction: Transaction): Map<String, Long> =
        accountDeltas(transaction).mapValues { (_, delta) -> -delta }
}
