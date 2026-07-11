package com.opfis.app.di

import com.opfis.domain.account.usecase.DeleteAccountUseCase
import com.opfis.domain.account.usecase.ObserveAccountsUseCase
import com.opfis.domain.account.usecase.UpsertAccountUseCase
import com.opfis.domain.asset.usecase.DeleteAssetUseCase
import com.opfis.domain.asset.usecase.ObserveAssetsUseCase
import com.opfis.domain.asset.usecase.UpsertAssetUseCase
import com.opfis.domain.budget.usecase.DeleteBudgetUseCase
import com.opfis.domain.budget.usecase.ObserveBudgetsUseCase
import com.opfis.domain.budget.usecase.UpsertBudgetUseCase
import com.opfis.domain.cashflow.usecase.ObserveCashFlowUseCase
import com.opfis.domain.category.usecase.DeleteCategoryUseCase
import com.opfis.domain.category.usecase.ObserveCategoriesUseCase
import com.opfis.domain.category.usecase.UpsertCategoryUseCase
import com.opfis.domain.goal.usecase.DeleteGoalUseCase
import com.opfis.domain.goal.usecase.ObserveGoalsUseCase
import com.opfis.domain.goal.usecase.UpsertGoalUseCase
import com.opfis.domain.liability.usecase.DeleteLiabilityUseCase
import com.opfis.domain.liability.usecase.ObserveLiabilitiesUseCase
import com.opfis.domain.liability.usecase.UpsertLiabilityUseCase
import com.opfis.domain.networth.usecase.ObserveNetWorthUseCase
import com.opfis.domain.search.usecase.SearchFinancialRecordsUseCase
import com.opfis.domain.systemstatus.usecase.ObserveSystemStatusUseCase
import com.opfis.domain.transaction.usecase.DeleteTransactionUseCase
import com.opfis.domain.transaction.usecase.ObserveAccountTransactionsUseCase
import com.opfis.domain.transaction.usecase.ObserveRecentTransactionsUseCase
import com.opfis.domain.transaction.usecase.ObserveTransactionsUseCase
import com.opfis.domain.transaction.usecase.RecordTransactionUseCase
import org.koin.dsl.module

/**
 * Composition-root bindings owned by the Presentation layer. Wires
 * Application-layer use cases on top of the repository bindings
 * `:data` provides - see ADR 0003.
 */
val appModule =
    module {
        factory { ObserveSystemStatusUseCase(repository = get()) }
        factory { ObserveAccountsUseCase(repository = get()) }
        factory { UpsertAccountUseCase(repository = get()) }
        factory { DeleteAccountUseCase(repository = get()) }
        factory { ObserveAssetsUseCase(repository = get()) }
        factory { UpsertAssetUseCase(repository = get()) }
        factory { DeleteAssetUseCase(repository = get()) }
        factory { ObserveLiabilitiesUseCase(repository = get()) }
        factory { UpsertLiabilityUseCase(repository = get()) }
        factory { DeleteLiabilityUseCase(repository = get()) }
        factory { ObserveCategoriesUseCase(repository = get()) }
        factory { UpsertCategoryUseCase(repository = get()) }
        factory { DeleteCategoryUseCase(repository = get()) }
        factory { ObserveBudgetsUseCase(repository = get()) }
        factory { UpsertBudgetUseCase(repository = get()) }
        factory { DeleteBudgetUseCase(repository = get()) }
        factory { ObserveGoalsUseCase(repository = get()) }
        factory { UpsertGoalUseCase(repository = get()) }
        factory { DeleteGoalUseCase(repository = get()) }
        factory { ObserveTransactionsUseCase(repository = get()) }
        factory { ObserveAccountTransactionsUseCase(repository = get()) }
        factory { ObserveRecentTransactionsUseCase(transactionRepository = get()) }
        factory { RecordTransactionUseCase(ledger = get()) }
        factory { DeleteTransactionUseCase(ledger = get()) }
        factory {
            ObserveNetWorthUseCase(
                accountRepository = get(),
                assetRepository = get(),
                liabilityRepository = get(),
            )
        }
        factory { ObserveCashFlowUseCase(transactionRepository = get()) }
        factory {
            SearchFinancialRecordsUseCase(
                accountRepository = get(),
                categoryRepository = get(),
                transactionRepository = get(),
            )
        }
    }
