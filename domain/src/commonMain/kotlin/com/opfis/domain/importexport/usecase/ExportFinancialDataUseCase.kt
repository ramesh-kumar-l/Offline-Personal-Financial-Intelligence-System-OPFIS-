package com.opfis.domain.importexport.usecase

import com.opfis.domain.importexport.FinancialDataSnapshot
import com.opfis.domain.importexport.ImportExportCoreRepositories
import com.opfis.domain.importexport.ImportExportRelatedRepositories
import com.opfis.domain.importexport.TransactionTagAssignment
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val prettyJson = Json { prettyPrint = true }

/**
 * Builds a full [FinancialDataSnapshot] from every repository and
 * serializes it to pretty-printed JSON (ROADMAP Phase 9, "JSON"
 * export) - the caller supplies [exportedAtEpochMillis] rather than
 * this use case reading a clock itself, matching this codebase's
 * convention of timestamps always coming from the call site (e.g.
 * `UpsertAccountUseCase` never computes `createdAt` either).
 */
class ExportFinancialDataUseCase(
    private val core: ImportExportCoreRepositories,
    private val related: ImportExportRelatedRepositories,
) {
    suspend operator fun invoke(exportedAtEpochMillis: Long): String {
        val tagAssignments =
            related.transactionTags
                .observeTagIdsByTransaction()
                .first()
                .flatMap { (transactionId, tagIds) ->
                    tagIds.map { tagId -> TransactionTagAssignment(transactionId, tagId) }
                }

        val snapshot =
            FinancialDataSnapshot(
                exportedAtEpochMillis = exportedAtEpochMillis,
                accounts = core.accounts.observeAll().first(),
                assets = core.assets.observeAll().first(),
                liabilities = core.liabilities.observeAll().first(),
                categories = core.categories.observeAll().first(),
                transactions = related.transactions.observeAll().first(),
                budgets = core.budgets.observeAll().first(),
                goals = core.goals.observeAll().first(),
                tags = core.tags.observeAll().first(),
                transactionTagAssignments = tagAssignments,
                documents = related.documents.observeAll().first(),
                memoryEvents = related.memoryEvents.observeAll().first(),
                relationships = related.relationships.observeAll().first(),
            )

        return prettyJson.encodeToString(snapshot)
    }
}
