package com.opfis.app.format

/**
 * Renders a minor-units [Long] (SystemPrompt Part 2: no floating point
 * anywhere in money handling) as a grouped decimal string, e.g.
 * `123456` -> `"1,234.56"`. No currency symbol is prefixed - Phase 2
 * deliberately has no multi-currency support (see `12-financial-engine.md`).
 */
object MoneyFormatter {
    private const val MINOR_UNITS_PER_MAJOR = 100L
    private const val GROUP_SIZE = 3

    fun format(amountMinorUnits: Long): String {
        val isNegative = amountMinorUnits < 0
        val absValue = if (isNegative) -amountMinorUnits else amountMinorUnits
        val majorUnits = absValue / MINOR_UNITS_PER_MAJOR
        val minorUnits = absValue % MINOR_UNITS_PER_MAJOR
        val sign = if (isNegative) "-" else ""
        return "$sign${groupThousands(majorUnits.toString())}.${minorUnits.toString().padStart(2, '0')}"
    }

    private fun groupThousands(digits: String): String {
        val builder = StringBuilder()
        for ((index, char) in digits.reversed().withIndex()) {
            if (index > 0 && index % GROUP_SIZE == 0) builder.append(',')
            builder.append(char)
        }
        return builder.reverse().toString()
    }
}
