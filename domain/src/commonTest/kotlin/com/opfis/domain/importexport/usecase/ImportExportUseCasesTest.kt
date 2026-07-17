package com.opfis.domain.importexport.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountType
import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetType
import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetPeriod
import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryType
import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentType
import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import com.opfis.domain.goal.Goal
import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityType
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventType
import com.opfis.domain.relationship.Relationship
import com.opfis.domain.relationship.RelationshipType
import com.opfis.domain.tag.Tag
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportExportUseCasesTest {
    @Test
    fun `export then import round-trips every entity type`() =
        runTest {
            val source = ImportExportFixture()
            seedEveryEntityType(source)

            val json = ExportFinancialDataUseCase(source.core, source.related).invoke(exportedAtEpochMillis = 42L)
            assertTrue(json.contains("acc-1"))
            assertTrue(json.contains("exportedAtEpochMillis"))

            val destination = ImportExportFixture()
            val summary = ImportFinancialDataUseCase(destination.core, destination.related)(json)

            assertEquals(listOf("acc-1"), destination.accounts.items.map { it.id })
            assertEquals(listOf("asset-1"), destination.assets.items.map { it.id })
            assertEquals(listOf("liab-1"), destination.liabilities.items.map { it.id })
            assertEquals(listOf("cat-1"), destination.categories.items.map { it.id })
            assertEquals(listOf("budget-1"), destination.budgets.items.map { it.id })
            assertEquals(listOf("goal-1"), destination.goals.items.map { it.id })
            assertEquals(listOf("tag-1"), destination.tags.items.map { it.id })
            assertEquals(listOf("tx-1"), destination.ledger.recorded.map { it.id })
            assertEquals(listOf("tx-1" to "tag-1"), destination.transactionTags.assigned)
            assertEquals(listOf("doc-1"), destination.documents.items.map { it.id })
            assertEquals(listOf("mem-1"), destination.memoryEvents.items.map { it.id })
            assertEquals(listOf("rel-1"), destination.relationships.items.map { it.id })

            assertEquals(1, summary.countsByEntity["Accounts"])
            assertEquals(1, summary.countsByEntity["Transactions"])
            assertEquals(1, summary.countsByEntity["Tag assignments"])
            assertEquals(1, summary.countsByEntity["Relationships"])
        }

    private fun seedEveryEntityType(source: ImportExportFixture) {
        seedCoreEntities(source)
        seedRelatedEntities(source)
    }

    private fun seedCoreEntities(source: ImportExportFixture) {
        source.accounts.items.add(
            Account("acc-1", "Checking", AccountType.CHECKING, 10_000L, createdAt = 0L, updatedAt = 0L),
        )
        source.assets.items.add(
            Asset("asset-1", "House", AssetType.REAL_ESTATE, 500_000L, createdAt = 0L, updatedAt = 0L),
        )
        source.liabilities.items.add(
            Liability("liab-1", "Car Loan", LiabilityType.LOAN, 20_000L, createdAt = 0L, updatedAt = 0L),
        )
        source.categories.items.add(
            Category("cat-1", "Groceries", CategoryType.EXPENSE, createdAt = 0L, updatedAt = 0L),
        )
        source.budgets.items.add(
            Budget("budget-1", "cat-1", 20_000L, BudgetPeriod.MONTHLY, 0L, createdAt = 0L, updatedAt = 0L),
        )
        source.goals.items.add(Goal("goal-1", "Emergency Fund", 100_000L, 25_000L, createdAt = 0L, updatedAt = 0L))
        source.tags.items.add(Tag("tag-1", "Essential", createdAt = 0L, updatedAt = 0L))
        source.transactions.items.add(
            Transaction(
                "tx-1",
                "acc-1",
                "cat-1",
                TransactionType.EXPENSE,
                5_000L,
                occurredAt = 0L,
                createdAt = 0L,
                updatedAt = 0L,
            ),
        )
        source.transactionTags.tagIdsByTransaction = mapOf("tx-1" to listOf("tag-1"))
    }

    private fun seedRelatedEntities(source: ImportExportFixture) {
        source.documents.items.add(
            Document(
                "doc-1",
                "receipt.pdf",
                "/path/receipt.pdf",
                "application/pdf",
                DocumentType.RECEIPT,
                "",
                importedAt = 0L,
                createdAt = 0L,
                updatedAt = 0L,
            ),
        )
        source.memoryEvents.items.add(
            MemoryEvent(
                "mem-1",
                MemoryEventType.NOTE,
                "Refinanced",
                "",
                subject = null,
                occurredAt = 0L,
                createdAt = 0L,
                updatedAt = 0L,
            ),
        )
        source.relationships.items.add(
            Relationship(
                "rel-1",
                EntityRef(EntityType.DOCUMENT, "doc-1"),
                EntityRef(EntityType.LIABILITY, "liab-1"),
                RelationshipType.SUPPORTING_DOCUMENT,
                createdAt = 0L,
                updatedAt = 0L,
            ),
        )
    }
}
