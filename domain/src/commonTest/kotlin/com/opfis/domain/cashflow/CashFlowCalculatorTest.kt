package com.opfis.domain.cashflow

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals

class CashFlowCalculatorTest {
    private val zone = TimeZone.UTC

    private fun epochMillis(
        year: Int,
        month: Int,
        day: Int,
    ) = LocalDate(year, month, day).atStartOfDayIn(zone).toEpochMilliseconds()

    private fun transaction(
        type: TransactionType,
        amount: Long,
        occurredAt: Long,
        transferAccountId: String? = null,
    ) = Transaction(
        id = "tx-$occurredAt",
        accountId = "acc-1",
        type = type,
        amountMinorUnits = amount,
        transferAccountId = transferAccountId,
        occurredAt = occurredAt,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `buckets income and expense into the correct calendar month`() {
        val transactions =
            listOf(
                transaction(TransactionType.INCOME, 5_000L, epochMillis(2026, 6, 15)),
                transaction(TransactionType.EXPENSE, 2_000L, epochMillis(2026, 6, 20)),
                transaction(TransactionType.INCOME, 1_000L, epochMillis(2026, 7, 1)),
            )

        val periods =
            CashFlowCalculator.summarizeByMonth(
                transactions,
                monthCount = 2,
                asOfEpochMillis = epochMillis(2026, 7, 12),
                zone = zone,
            )

        assertEquals(listOf(2026 to 6, 2026 to 7), periods.map { it.year to it.month })
        assertEquals(5_000L, periods[0].incomeMinorUnits)
        assertEquals(2_000L, periods[0].expenseMinorUnits)
        assertEquals(1_000L, periods[1].incomeMinorUnits)
        assertEquals(0L, periods[1].expenseMinorUnits)
    }

    @Test
    fun `transfers do not affect income or expense totals`() {
        val transactions =
            listOf(transaction(TransactionType.TRANSFER, 4_000L, epochMillis(2026, 7, 5), transferAccountId = "acc-2"))

        val periods =
            CashFlowCalculator.summarizeByMonth(
                transactions,
                monthCount = 1,
                asOfEpochMillis = epochMillis(2026, 7, 12),
                zone = zone,
            )

        assertEquals(0L, periods.single().incomeMinorUnits)
        assertEquals(0L, periods.single().expenseMinorUnits)
    }

    @Test
    fun `months with no transactions still appear with zero totals`() {
        val periods =
            CashFlowCalculator.summarizeByMonth(
                emptyList(),
                monthCount = 3,
                asOfEpochMillis = epochMillis(2026, 3, 10),
                zone = zone,
            )

        assertEquals(listOf(2026 to 1, 2026 to 2, 2026 to 3), periods.map { it.year to it.month })
        assertEquals(true, periods.all { it.incomeMinorUnits == 0L && it.expenseMinorUnits == 0L })
    }

    @Test
    fun `year boundary rolls over correctly`() {
        val periods =
            CashFlowCalculator.summarizeByMonth(
                emptyList(),
                monthCount = 3,
                asOfEpochMillis = epochMillis(2026, 1, 15),
                zone = zone,
            )

        assertEquals(listOf(2025 to 11, 2025 to 12, 2026 to 1), periods.map { it.year to it.month })
    }

    @Test
    fun `transactions outside the requested window are ignored`() {
        val transactions = listOf(transaction(TransactionType.INCOME, 9_999L, epochMillis(2020, 1, 1)))

        val periods =
            CashFlowCalculator.summarizeByMonth(
                transactions,
                monthCount = 1,
                asOfEpochMillis = epochMillis(2026, 7, 12),
                zone = zone,
            )

        assertEquals(0L, periods.single().incomeMinorUnits)
    }
}
