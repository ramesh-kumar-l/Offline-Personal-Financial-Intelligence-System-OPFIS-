package com.opfis.domain.cashflow

/**
 * Aggregated income/expense totals for one calendar month
 * (SystemPrompt Part 3, "Charts": Cash Flow). [month] is 1-12.
 * TRANSFER transactions never contribute here - they move money
 * between the user's own accounts and are cash-flow neutral.
 */
data class CashFlowPeriod(
    val year: Int,
    val month: Int,
    val incomeMinorUnits: Long,
    val expenseMinorUnits: Long,
) {
    val netMinorUnits: Long get() = incomeMinorUnits - expenseMinorUnits
}
