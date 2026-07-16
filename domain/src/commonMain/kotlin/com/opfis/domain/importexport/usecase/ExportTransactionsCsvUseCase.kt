package com.opfis.domain.importexport.usecase

import com.opfis.domain.importexport.TransactionCsvCodec
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.first

/** Exports every transaction as CSV (ROADMAP Phase 9, "CSV"). */
class ExportTransactionsCsvUseCase(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(): String = TransactionCsvCodec.encode(transactionRepository.observeAll().first())
}
