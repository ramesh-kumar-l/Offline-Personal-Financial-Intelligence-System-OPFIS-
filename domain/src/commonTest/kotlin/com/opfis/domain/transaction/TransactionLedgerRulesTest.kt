package com.opfis.domain.transaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransactionLedgerRulesTest {
    private fun transaction(
        type: TransactionType,
        amount: Long = 1_000L,
        accountId: String = "acc-1",
        transferAccountId: String? = null,
    ) = Transaction(
        id = "tx-1",
        accountId = accountId,
        type = type,
        amountMinorUnits = amount,
        transferAccountId = transferAccountId,
        occurredAt = 0L,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `income credits the source account`() {
        val deltas = TransactionLedgerRules.accountDeltas(transaction(TransactionType.INCOME, amount = 500L))
        assertEquals(mapOf("acc-1" to 500L), deltas)
    }

    @Test
    fun `expense debits the source account`() {
        val deltas = TransactionLedgerRules.accountDeltas(transaction(TransactionType.EXPENSE, amount = 500L))
        assertEquals(mapOf("acc-1" to -500L), deltas)
    }

    @Test
    fun `transfer debits source and credits destination`() {
        val deltas =
            TransactionLedgerRules.accountDeltas(
                transaction(TransactionType.TRANSFER, amount = 500L, transferAccountId = "acc-2"),
            )
        assertEquals(mapOf("acc-1" to -500L, "acc-2" to 500L), deltas)
    }

    @Test
    fun `reversal deltas invert the original deltas`() {
        val original = transaction(TransactionType.TRANSFER, amount = 500L, transferAccountId = "acc-2")
        val forward = TransactionLedgerRules.accountDeltas(original)
        val reversed = TransactionLedgerRules.reversalDeltas(original)
        assertEquals(forward.mapValues { (_, v) -> -v }, reversed)
    }

    @Test
    fun `transfer requires a destination account`() {
        assertFailsWith<IllegalArgumentException> {
            transaction(TransactionType.TRANSFER, transferAccountId = null)
        }
    }

    @Test
    fun `transfer cannot target its own source account`() {
        assertFailsWith<IllegalArgumentException> {
            transaction(TransactionType.TRANSFER, transferAccountId = "acc-1")
        }
    }

    @Test
    fun `non-transfer types must not carry a transfer account`() {
        assertFailsWith<IllegalArgumentException> {
            transaction(TransactionType.INCOME, transferAccountId = "acc-2")
        }
    }

    @Test
    fun `amount must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            transaction(TransactionType.INCOME, amount = 0L)
        }
    }
}
