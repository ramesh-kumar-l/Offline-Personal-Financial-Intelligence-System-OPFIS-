package com.opfis.app.format

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Formats epoch-millisecond timestamps (`Transaction.occurredAt`) for display. */
object DateFormatter {
    fun formatDay(
        epochMillis: Long,
        zone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val dateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
        return "${dateTime.dayOfMonth} ${MonthLabelFormatter.abbreviate(dateTime.monthNumber)} ${dateTime.year}"
    }
}
