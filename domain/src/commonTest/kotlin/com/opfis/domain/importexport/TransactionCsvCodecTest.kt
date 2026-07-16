package com.opfis.domain.importexport

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionCsvCodecTest {
    @Test
    fun `encode then decode round-trips a plain transaction`() {
        val transaction =
            Transaction(
                id = "tx-1",
                accountId = "acc-1",
                categoryId = "cat-1",
                type = TransactionType.EXPENSE,
                amountMinorUnits = 5_000L,
                description = "Groceries",
                occurredAt = 1_000L,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            )

        val decoded = TransactionCsvCodec.decode(TransactionCsvCodec.encode(listOf(transaction)))

        assertEquals(listOf(transaction), decoded)
    }

    @Test
    fun `a description with a comma, quote, and newline round-trips correctly`() {
        val transaction =
            Transaction(
                id = "tx-1",
                accountId = "acc-1",
                type = TransactionType.INCOME,
                amountMinorUnits = 1_000L,
                description = "Refund, \"partial\"\nsecond line",
                occurredAt = 1_000L,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            )

        val encoded = TransactionCsvCodec.encode(listOf(transaction))
        val decoded = TransactionCsvCodec.decode(encoded)

        assertEquals(1, encoded.lines().filter { it.isNotBlank() }.size - 1)
        assertEquals("Refund, \"partial\" second line", decoded.single().description)
    }

    @Test
    fun `null categoryId and transferAccountId round-trip as null`() {
        val transaction =
            Transaction(
                id = "tx-1",
                accountId = "acc-1",
                categoryId = null,
                type = TransactionType.INCOME,
                amountMinorUnits = 2_000L,
                transferAccountId = null,
                occurredAt = 1_000L,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            )

        val decoded = TransactionCsvCodec.decode(TransactionCsvCodec.encode(listOf(transaction))).single()

        assertEquals(null, decoded.categoryId)
        assertEquals(null, decoded.transferAccountId)
    }

    @Test
    fun `decoding an empty or header-only CSV yields no transactions`() {
        assertTrue(TransactionCsvCodec.decode("").isEmpty())
        assertTrue(TransactionCsvCodec.decode(TransactionCsvCodec.encode(emptyList())).isEmpty())
    }
}
