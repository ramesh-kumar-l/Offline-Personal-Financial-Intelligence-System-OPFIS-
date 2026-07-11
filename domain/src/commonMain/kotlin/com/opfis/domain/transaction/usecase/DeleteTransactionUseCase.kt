package com.opfis.domain.transaction.usecase

import com.opfis.domain.transaction.FinancialLedgerPort

class DeleteTransactionUseCase(
    private val ledger: FinancialLedgerPort,
) {
    suspend operator fun invoke(transactionId: String) = ledger.deleteTransaction(transactionId)
}
