package com.opfis.data.transaction

import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.transaction.FinancialLedgerPort
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionLedgerRules

/**
 * Posts/reverses a transaction and its [TransactionLedgerRules] balance
 * deltas as one SQLDelight `transaction {}` block, so a crash mid-write
 * can never leave a transaction row without its matching balance
 * adjustment (SYSTEM_PROMPT Part 2: writes must be atomic).
 */
class SqlFinancialLedger(
    private val database: OpfisDatabase,
) : FinancialLedgerPort {
    override suspend fun recordTransaction(transaction: Transaction) {
        database.financialTransactionQueries.transaction {
            val existingVersion =
                database.financialTransactionQueries
                    .selectById(transaction.id)
                    .executeAsOneOrNull()
                    ?.version ?: 0
            database.financialTransactionQueries.insertOrReplace(
                id = transaction.id,
                account_id = transaction.accountId,
                category_id = transaction.categoryId,
                type = transaction.type.name,
                amount_minor_units = transaction.amountMinorUnits,
                transfer_account_id = transaction.transferAccountId,
                description = transaction.description,
                occurred_at = transaction.occurredAt,
                created_at = transaction.createdAt,
                updated_at = transaction.updatedAt,
                version = existingVersion + 1,
            )
            TransactionLedgerRules.accountDeltas(transaction).forEach { (accountId, delta) ->
                database.accountQueries.adjustBalance(delta, transaction.updatedAt, accountId)
            }
        }
    }

    override suspend fun deleteTransaction(transactionId: String) {
        database.financialTransactionQueries.transaction {
            val row = database.financialTransactionQueries.selectById(transactionId).executeAsOneOrNull()
            if (row == null) {
                return@transaction
            }
            val transaction = toDomainTransaction(row)
            database.financialTransactionQueries.deleteById(transactionId)
            TransactionLedgerRules.reversalDeltas(transaction).forEach { (accountId, delta) ->
                database.accountQueries.adjustBalance(delta, transaction.updatedAt, accountId)
            }
        }
    }
}
