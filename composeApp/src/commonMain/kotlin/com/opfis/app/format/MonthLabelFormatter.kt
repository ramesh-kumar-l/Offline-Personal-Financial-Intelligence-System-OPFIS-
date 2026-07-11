package com.opfis.app.format

/**
 * Formats the (year, month) pairs used by `CashFlowPeriod` into short,
 * locale-independent labels for chart axes (e.g. `"Jul 2026"`).
 */
object MonthLabelFormatter {
    private val ABBREVIATIONS =
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    fun format(
        year: Int,
        month: Int,
    ): String {
        require(month in 1..12) { "month must be in 1..12" }
        return "${ABBREVIATIONS[month - 1]} $year"
    }

    fun abbreviate(month: Int): String {
        require(month in 1..12) { "month must be in 1..12" }
        return ABBREVIATIONS[month - 1]
    }
}
