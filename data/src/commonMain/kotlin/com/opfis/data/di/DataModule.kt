package com.opfis.data.di

import com.opfis.data.account.SqlAccountRepository
import com.opfis.data.asset.SqlAssetRepository
import com.opfis.data.audit.SqlAuditLogRepository
import com.opfis.data.budget.SqlBudgetRepository
import com.opfis.data.category.SqlCategoryRepository
import com.opfis.data.document.SqlDocumentRepository
import com.opfis.data.goal.SqlGoalRepository
import com.opfis.data.liability.SqlLiabilityRepository
import com.opfis.data.memory.SqlMemoryEventRepository
import com.opfis.data.relationship.SqlRelationshipRepository
import com.opfis.data.search.SqlSearchIndexRepository
import com.opfis.data.systemstatus.PersistentSystemStatusRepository
import com.opfis.data.tag.SqlTagRepository
import com.opfis.data.tag.SqlTransactionTagRepository
import com.opfis.data.transaction.SqlFinancialLedger
import com.opfis.data.transaction.SqlTransactionRepository
import com.opfis.domain.account.AccountRepository
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.audit.AuditLogRepository
import com.opfis.domain.budget.BudgetRepository
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.document.DocumentRepository
import com.opfis.domain.goal.GoalRepository
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.memory.MemoryEventRepository
import com.opfis.domain.relationship.RelationshipRepository
import com.opfis.domain.search.SearchPort
import com.opfis.domain.systemstatus.SystemStatusRepository
import com.opfis.domain.tag.TagRepository
import com.opfis.domain.tag.TransactionTagRepository
import com.opfis.domain.transaction.FinancialLedgerPort
import com.opfis.domain.transaction.TransactionRepository
import com.opfis.shared.logging.Logger
import com.opfis.shared.logging.platformLogger
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin bindings owned by the Infrastructure layer that don't need a
 * platform `Context`/directory. The database driver, key provider, and
 * backup port are registered by the platform-specific
 * `androidDataModule` / `desktopDataModule` (see ADR 0005) and loaded
 * alongside this module from the composition root.
 */
val dataModule =
    module {
        single<Logger> { platformLogger() }
        singleOf(::PersistentSystemStatusRepository) { bind<SystemStatusRepository>() }
        singleOf(::SqlAccountRepository) { bind<AccountRepository>() }
        singleOf(::SqlAssetRepository) { bind<AssetRepository>() }
        singleOf(::SqlLiabilityRepository) { bind<LiabilityRepository>() }
        singleOf(::SqlCategoryRepository) { bind<CategoryRepository>() }
        singleOf(::SqlBudgetRepository) { bind<BudgetRepository>() }
        singleOf(::SqlGoalRepository) { bind<GoalRepository>() }
        singleOf(::SqlTransactionRepository) { bind<TransactionRepository>() }
        singleOf(::SqlFinancialLedger) { bind<FinancialLedgerPort>() }
        singleOf(::SqlTagRepository) { bind<TagRepository>() }
        singleOf(::SqlTransactionTagRepository) { bind<TransactionTagRepository>() }
        singleOf(::SqlSearchIndexRepository) { bind<SearchPort>() }
        singleOf(::SqlDocumentRepository) { bind<DocumentRepository>() }
        singleOf(::SqlMemoryEventRepository) { bind<MemoryEventRepository>() }
        singleOf(::SqlRelationshipRepository) { bind<RelationshipRepository>() }
        singleOf(::SqlAuditLogRepository) { bind<AuditLogRepository>() }
    }
