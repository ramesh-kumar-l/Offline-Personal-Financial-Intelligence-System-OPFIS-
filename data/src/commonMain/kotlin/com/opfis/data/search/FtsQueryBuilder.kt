package com.opfis.data.search

/**
 * Turns free-text user input into a SQLite FTS5 MATCH expression: each
 * whitespace-separated token becomes a quoted prefix match, ANDed
 * together, so "gro rent" finds rows containing both a "gro*" and a
 * "rent*" token (instant, as-you-type search).
 */
internal object FtsQueryBuilder {
    fun toMatchExpression(rawQuery: String): String? {
        val tokens = rawQuery.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return null
        return tokens.joinToString(separator = " AND ") { token ->
            val escaped = token.replace("\"", "\"\"")
            "\"$escaped\"*"
        }
    }
}
