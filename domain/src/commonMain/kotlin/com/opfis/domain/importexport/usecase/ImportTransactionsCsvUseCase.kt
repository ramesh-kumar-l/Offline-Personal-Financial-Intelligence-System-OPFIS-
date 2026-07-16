package com.opfis.domain.importexport.usecase

import com.opfis.domain.importexport.TransactionCsvCodec
import com.opfis.domain.transaction.FinancialLedgerPort

/**
 * Imports transactions from CSV (ROADMAP Phase 9, "CSV"), posting each
 * through [FinancialLedgerPort] rather than a raw upsert so account
 * balances stay consistent. Returns the number of rows imported.
 */
class ImportTransactionsCsvUseCase(
    private val ledger: FinancialLedgerPort,
) {
    suspend operator fun invoke(csv: String): Int {
        val transactions = TransactionCsvCodec.decode(csv)
        transactions.forEach { ledger.recordTransaction(it) }
        return transactions.size
    }
}
