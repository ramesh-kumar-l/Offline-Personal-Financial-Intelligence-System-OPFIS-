package com.opfis.domain.importexport.usecase

import com.opfis.domain.importexport.FinancialDataSnapshot
import com.opfis.domain.importexport.ImportExportCoreRepositories
import com.opfis.domain.importexport.ImportExportRelatedRepositories
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Per-entity row counts from a completed import, for UI feedback. */
data class ImportSummary(
    val countsByEntity: Map<String, Int>,
)

/**
 * Replays a [FinancialDataSnapshot] (ROADMAP Phase 9, "JSON" import).
 * Writes independent entities first, then transactions via
 * [ImportExportRelatedRepositories.ledger] (never a raw upsert - it
 * also keeps account balances consistent), then the relational/
 * dependent entities - see `26-active-initiatives.md` for the full
 * ordering rationale. `id`-preserving upsert/`INSERT OR REPLACE`
 * semantics mean re-importing the same file is idempotent. Document
 * rows are metadata/extracted-text only; a restored [Document.storagePath]
 * may point at a file the user hasn't re-supplied.
 */
class ImportFinancialDataUseCase(
    private val core: ImportExportCoreRepositories,
    private val related: ImportExportRelatedRepositories,
) {
    suspend operator fun invoke(json: String): ImportSummary {
        val snapshot = Json.decodeFromString<FinancialDataSnapshot>(json)

        snapshot.accounts.forEach { core.accounts.upsert(it) }
        snapshot.assets.forEach { core.assets.upsert(it) }
        snapshot.liabilities.forEach { core.liabilities.upsert(it) }
        snapshot.categories.forEach { core.categories.upsert(it) }
        snapshot.tags.forEach { core.tags.upsert(it) }
        snapshot.budgets.forEach { core.budgets.upsert(it) }
        snapshot.goals.forEach { core.goals.upsert(it) }

        snapshot.transactions.forEach { related.ledger.recordTransaction(it) }
        snapshot.transactionTagAssignments.forEach { related.transactionTags.assignTag(it.transactionId, it.tagId) }
        snapshot.documents.forEach { related.documents.upsert(it) }
        snapshot.memoryEvents.forEach { related.memoryEvents.upsert(it) }
        snapshot.relationships.forEach { related.relationships.upsert(it) }

        return ImportSummary(
            countsByEntity =
                mapOf(
                    "Accounts" to snapshot.accounts.size,
                    "Assets" to snapshot.assets.size,
                    "Liabilities" to snapshot.liabilities.size,
                    "Categories" to snapshot.categories.size,
                    "Transactions" to snapshot.transactions.size,
                    "Budgets" to snapshot.budgets.size,
                    "Goals" to snapshot.goals.size,
                    "Tags" to snapshot.tags.size,
                    "Tag assignments" to snapshot.transactionTagAssignments.size,
                    "Documents" to snapshot.documents.size,
                    "Memory events" to snapshot.memoryEvents.size,
                    "Relationships" to snapshot.relationships.size,
                ),
        )
    }
}
