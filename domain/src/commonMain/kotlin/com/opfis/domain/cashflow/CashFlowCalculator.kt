package com.opfis.domain.cashflow

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Pure domain policy (no SQL/framework dependency) bucketing posted
 * transactions into calendar-month income/expense totals. Uses
 * kotlinx-datetime rather than hand-rolled arithmetic so month/year
 * rollover (28-31 day months, year boundaries) is always correct.
 */
object CashFlowCalculator {
    fun summarizeByMonth(
        transactions: List<Transaction>,
        monthCount: Int,
        asOfEpochMillis: Long,
        zone: TimeZone = TimeZone.currentSystemDefault(),
    ): List<CashFlowPeriod> {
        require(monthCount > 0) { "monthCount must be positive" }

        val asOf = Instant.fromEpochMilliseconds(asOfEpochMillis).toLocalDateTime(zone)
        val periodKeys = (monthCount - 1 downTo 0).map { offset -> shiftMonth(asOf.year, asOf.monthNumber, -offset) }
        val totals = LinkedHashMap<Pair<Int, Int>, CashFlowPeriod>()
        periodKeys.forEach { key -> totals[key] = CashFlowPeriod(key.first, key.second, 0L, 0L) }

        transactions
            .filter { it.type != TransactionType.TRANSFER }
            .forEach { transaction ->
                val occurred = Instant.fromEpochMilliseconds(transaction.occurredAt).toLocalDateTime(zone)
                val key = occurred.year to occurred.monthNumber
                val existing = totals[key] ?: return@forEach
                totals[key] =
                    when (transaction.type) {
                        TransactionType.INCOME ->
                            existing.copy(incomeMinorUnits = existing.incomeMinorUnits + transaction.amountMinorUnits)
                        TransactionType.EXPENSE ->
                            existing.copy(expenseMinorUnits = existing.expenseMinorUnits + transaction.amountMinorUnits)
                        TransactionType.TRANSFER -> existing
                    }
            }

        return periodKeys.map { totals.getValue(it) }
    }

    private fun shiftMonth(
        year: Int,
        month: Int,
        deltaMonths: Int,
    ): Pair<Int, Int> {
        val zeroBasedMonth = (month - 1) + deltaMonths
        val yearOffset = floorDiv(zeroBasedMonth, MONTHS_PER_YEAR)
        val newMonth = floorMod(zeroBasedMonth, MONTHS_PER_YEAR) + 1
        return (year + yearOffset) to newMonth
    }

    private fun floorDiv(
        x: Int,
        y: Int,
    ): Int {
        val quotient = x / y
        return if ((x xor y) < 0 && quotient * y != x) quotient - 1 else quotient
    }

    private fun floorMod(
        x: Int,
        y: Int,
    ): Int = x - floorDiv(x, y) * y

    private const val MONTHS_PER_YEAR = 12
}
