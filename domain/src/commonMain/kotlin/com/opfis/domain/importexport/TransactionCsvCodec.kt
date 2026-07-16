package com.opfis.domain.importexport

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType

private const val CSV_HEADER =
    "id,accountId,categoryId,type,amountMinorUnits,transferAccountId,description,occurredAt,createdAt,updatedAt"

/**
 * Lossless, id-based CSV encoding for [Transaction] (ROADMAP Phase 9,
 * "CSV" - scoped to transactions, the one entity with a natural
 * tabular shape). Columns mirror [Transaction]'s own fields 1:1 rather
 * than human-readable account/category names, so import never has to
 * resolve an ambiguous name -> id lookup. Any newline embedded in
 * [Transaction.description] is normalized to a space at encode time so
 * every row is guaranteed to be exactly one physical line - this codec
 * deliberately doesn't implement full RFC4180 multi-line quoted fields.
 */
object TransactionCsvCodec {
    fun encode(transactions: List<Transaction>): String {
        val rows = transactions.joinToString(separator = "\n") { toCsvRow(it) }
        return if (rows.isEmpty()) CSV_HEADER else "$CSV_HEADER\n$rows"
    }

    fun decode(csv: String): List<Transaction> {
        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        return lines.drop(1).map(::toTransaction)
    }

    private fun toCsvRow(transaction: Transaction): String =
        listOf(
            transaction.id,
            transaction.accountId,
            transaction.categoryId.orEmpty(),
            transaction.type.name,
            transaction.amountMinorUnits.toString(),
            transaction.transferAccountId.orEmpty(),
            transaction.description.replace("\r", " ").replace("\n", " "),
            transaction.occurredAt.toString(),
            transaction.createdAt.toString(),
            transaction.updatedAt.toString(),
        ).joinToString(separator = ",") { escapeCsvField(it) }

    private fun toTransaction(line: String): Transaction {
        val fields = parseCsvLine(line)
        return Transaction(
            id = fields[0],
            accountId = fields[1],
            categoryId = fields[2].ifEmpty { null },
            type = TransactionType.valueOf(fields[3]),
            amountMinorUnits = fields[4].toLong(),
            transferAccountId = fields[5].ifEmpty { null },
            description = fields[6],
            occurredAt = fields[7].toLong(),
            createdAt = fields[8].toLong(),
            updatedAt = fields[9].toLong(),
        )
    }

    private fun escapeCsvField(field: String): String =
        if (field.any { it == ',' || it == '"' }) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                inQuotes && c == '"' -> inQuotes = false
                !inQuotes && c == '"' -> inQuotes = true
                !inQuotes && c == ',' -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
