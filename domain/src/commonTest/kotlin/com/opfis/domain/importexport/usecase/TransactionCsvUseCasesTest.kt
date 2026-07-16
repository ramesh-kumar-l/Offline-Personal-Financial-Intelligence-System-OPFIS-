package com.opfis.domain.importexport.usecase

import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionCsvUseCasesTest {
    @Test
    fun `export then import round-trips transactions through CSV`() =
        runTest {
            val source = FakeTransactionRepository()
            source.items.add(
                Transaction("tx-1", "acc-1", "cat-1", TransactionType.EXPENSE, 5_000L, occurredAt = 0L, createdAt = 0L, updatedAt = 0L),
            )

            val csv = ExportTransactionsCsvUseCase(source)()

            val ledger = FakeFinancialLedgerPort()
            val count = ImportTransactionsCsvUseCase(ledger)(csv)

            assertEquals(1, count)
            assertEquals(listOf("tx-1"), ledger.recorded.map { it.id })
            assertEquals(source.items, ledger.recorded)
        }
}
