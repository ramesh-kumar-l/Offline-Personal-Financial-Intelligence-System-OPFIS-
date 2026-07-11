package com.opfis.domain.cashflow.usecase

import com.opfis.domain.cashflow.CashFlowCalculator
import com.opfis.domain.cashflow.CashFlowPeriod
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Application-layer use case: the last [monthCount] months of
 * income/expense totals, recomputed whenever the transaction ledger
 * changes.
 */
class ObserveCashFlowUseCase(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(monthCount: Int = DEFAULT_MONTH_COUNT): Flow<List<CashFlowPeriod>> {
        val asOfEpochMillis = Clock.System.now().toEpochMilliseconds()
        return transactionRepository.observeAll().map { transactions ->
            CashFlowCalculator.summarizeByMonth(transactions, monthCount, asOfEpochMillis)
        }
    }

    companion object {
        const val DEFAULT_MONTH_COUNT = 6
    }
}
