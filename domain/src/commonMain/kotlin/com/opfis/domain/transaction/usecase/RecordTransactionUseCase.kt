package com.opfis.domain.transaction.usecase

import com.opfis.domain.transaction.FinancialLedgerPort
import com.opfis.domain.transaction.Transaction

class RecordTransactionUseCase(
    private val ledger: FinancialLedgerPort,
) {
    suspend operator fun invoke(transaction: Transaction) = ledger.recordTransaction(transaction)
}
