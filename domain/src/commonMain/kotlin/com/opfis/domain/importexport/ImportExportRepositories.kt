package com.opfis.domain.importexport

import com.opfis.domain.account.AccountRepository
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.budget.BudgetRepository
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.document.DocumentRepository
import com.opfis.domain.goal.GoalRepository
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.memory.MemoryEventRepository
import com.opfis.domain.relationship.RelationshipRepository
import com.opfis.domain.tag.TagRepository
import com.opfis.domain.tag.TransactionTagRepository
import com.opfis.domain.transaction.FinancialLedgerPort
import com.opfis.domain.transaction.TransactionRepository

/**
 * Bundles the independent Phase 2 entity repositories + tags into one
 * constructor parameter, so [usecase.ExportFinancialDataUseCase]/
 * [usecase.ImportFinancialDataUseCase] stay under detekt's
 * `LongParameterList` threshold (same technique as
 * `com.opfis.domain.ai.FinancialRepositories`).
 */
data class ImportExportCoreRepositories(
    val accounts: AccountRepository,
    val assets: AssetRepository,
    val liabilities: LiabilityRepository,
    val categories: CategoryRepository,
    val budgets: BudgetRepository,
    val goals: GoalRepository,
    val tags: TagRepository,
)

/** The relational/write-side ports the core bundle above doesn't cover. */
data class ImportExportRelatedRepositories(
    val transactions: TransactionRepository,
    val transactionTags: TransactionTagRepository,
    val documents: DocumentRepository,
    val memoryEvents: MemoryEventRepository,
    val relationships: RelationshipRepository,
    val ledger: FinancialLedgerPort,
)
