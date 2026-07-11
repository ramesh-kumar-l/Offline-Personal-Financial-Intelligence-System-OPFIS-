package com.opfis.data.di

import com.opfis.data.account.SqlAccountRepository
import com.opfis.data.asset.SqlAssetRepository
import com.opfis.data.budget.SqlBudgetRepository
import com.opfis.data.category.SqlCategoryRepository
import com.opfis.data.goal.SqlGoalRepository
import com.opfis.data.liability.SqlLiabilityRepository
import com.opfis.data.systemstatus.PersistentSystemStatusRepository
import com.opfis.data.transaction.SqlFinancialLedger
import com.opfis.data.transaction.SqlTransactionRepository
import com.opfis.domain.account.AccountRepository
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.budget.BudgetRepository
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.goal.GoalRepository
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.systemstatus.SystemStatusRepository
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
    }
