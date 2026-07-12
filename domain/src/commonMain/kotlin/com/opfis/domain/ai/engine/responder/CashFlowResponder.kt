package com.opfis.domain.ai.engine.responder

import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.AiMoneyFormatter
import com.opfis.domain.ai.FinancialSnapshot
import com.opfis.domain.cashflow.CashFlowCalculator

/** Answers CASH_FLOW questions with the current month's income/expense/net, reusing Phase 3's [CashFlowCalculator]. */
internal object CashFlowResponder {
    fun respond(
        snapshot: FinancialSnapshot,
        asOfEpochMillis: Long,
    ): AiAnswer {
        val period = CashFlowCalculator.summarizeByMonth(snapshot.transactions, MONTH_COUNT, asOfEpochMillis).first()
        val text =
            "This month: income ${AiMoneyFormatter.format(period.incomeMinorUnits)}, " +
                "expenses ${AiMoneyFormatter.format(period.expenseMinorUnits)}, " +
                "net ${AiMoneyFormatter.format(period.netMinorUnits)}."
        return AiAnswer(text)
    }

    private const val MONTH_COUNT = 1
}
