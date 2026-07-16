package com.opfis.app.di

import com.opfis.domain.account.usecase.DeleteAccountUseCase
import com.opfis.domain.account.usecase.ObserveAccountsUseCase
import com.opfis.domain.account.usecase.UpsertAccountUseCase
import com.opfis.domain.ai.FinancialRepositories
import com.opfis.domain.ai.LocalAiPort
import com.opfis.domain.ai.engine.RuleBasedLocalAiEngine
import com.opfis.domain.ai.usecase.AskAiAssistantUseCase
import com.opfis.domain.ai.usecase.BuildFinancialSnapshotUseCase
import com.opfis.domain.ai.usecase.RetrieveFinancialContextUseCase
import com.opfis.domain.asset.usecase.DeleteAssetUseCase
import com.opfis.domain.asset.usecase.ObserveAssetsUseCase
import com.opfis.domain.asset.usecase.UpsertAssetUseCase
import com.opfis.domain.audit.usecase.ObserveAuditLogUseCase
import com.opfis.domain.audit.usecase.RecordAuditEventUseCase
import com.opfis.domain.backup.usecase.ExportBackupUseCase
import com.opfis.domain.backup.usecase.RestoreBackupUseCase
import com.opfis.domain.budget.usecase.DeleteBudgetUseCase
import com.opfis.domain.budget.usecase.ObserveBudgetsUseCase
import com.opfis.domain.budget.usecase.UpsertBudgetUseCase
import com.opfis.domain.cashflow.usecase.ObserveCashFlowUseCase
import com.opfis.domain.category.usecase.DeleteCategoryUseCase
import com.opfis.domain.category.usecase.ObserveCategoriesUseCase
import com.opfis.domain.category.usecase.UpsertCategoryUseCase
import com.opfis.domain.document.usecase.DeleteDocumentUseCase
import com.opfis.domain.document.usecase.ImportDocumentUseCase
import com.opfis.domain.document.usecase.LinkDocumentToTransactionUseCase
import com.opfis.domain.document.usecase.ObserveDocumentsForTransactionUseCase
import com.opfis.domain.document.usecase.ObserveDocumentsUseCase
import com.opfis.domain.goal.usecase.DeleteGoalUseCase
import com.opfis.domain.goal.usecase.ObserveGoalsUseCase
import com.opfis.domain.goal.usecase.UpsertGoalUseCase
import com.opfis.domain.importexport.ImportExportCoreRepositories
import com.opfis.domain.importexport.ImportExportRelatedRepositories
import com.opfis.domain.importexport.usecase.ExportFinancialDataUseCase
import com.opfis.domain.importexport.usecase.ExportTransactionsCsvUseCase
import com.opfis.domain.importexport.usecase.ImportFinancialDataUseCase
import com.opfis.domain.importexport.usecase.ImportTransactionsCsvUseCase
import com.opfis.domain.liability.usecase.DeleteLiabilityUseCase
import com.opfis.domain.liability.usecase.ObserveLiabilitiesUseCase
import com.opfis.domain.liability.usecase.UpsertLiabilityUseCase
import com.opfis.domain.memory.usecase.DeleteMemoryEventUseCase
import com.opfis.domain.memory.usecase.ObserveMemoryEventsForEntityUseCase
import com.opfis.domain.memory.usecase.ObserveMemoryTimelineUseCase
import com.opfis.domain.memory.usecase.RecordMemoryEventUseCase
import com.opfis.domain.networth.usecase.ObserveNetWorthUseCase
import com.opfis.domain.relationship.usecase.CreateRelationshipUseCase
import com.opfis.domain.relationship.usecase.DeleteRelationshipUseCase
import com.opfis.domain.relationship.usecase.ObserveKnowledgeGraphUseCase
import com.opfis.domain.relationship.usecase.ObserveRelationshipsForEntityUseCase
import com.opfis.domain.search.usecase.SearchFinancialRecordsUseCase
import com.opfis.domain.systemstatus.usecase.ObserveSystemStatusUseCase
import com.opfis.domain.tag.usecase.AssignTagToTransactionUseCase
import com.opfis.domain.tag.usecase.DeleteTagUseCase
import com.opfis.domain.tag.usecase.ObserveTagsUseCase
import com.opfis.domain.tag.usecase.ObserveTransactionTagsUseCase
import com.opfis.domain.tag.usecase.RemoveTagFromTransactionUseCase
import com.opfis.domain.tag.usecase.UpsertTagUseCase
import com.opfis.domain.timeline.usecase.ObserveTimelineUseCase
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
        factory { SearchFinancialRecordsUseCase(searchPort = get()) }
        factory { ObserveTagsUseCase(repository = get()) }
        factory { UpsertTagUseCase(repository = get()) }
        factory { DeleteTagUseCase(repository = get()) }
        factory { ObserveTransactionTagsUseCase(repository = get()) }
        factory { AssignTagToTransactionUseCase(repository = get()) }
        factory { RemoveTagFromTransactionUseCase(repository = get()) }
        factory {
            ObserveTimelineUseCase(
                transactionRepository = get(),
                transactionTagRepository = get(),
            )
        }
        factory { ObserveDocumentsUseCase(repository = get()) }
        factory { ObserveDocumentsForTransactionUseCase(repository = get()) }
        factory { LinkDocumentToTransactionUseCase(repository = get()) }
        factory { DeleteDocumentUseCase(repository = get(), storage = get()) }
        factory {
            ImportDocumentUseCase(
                documentRepository = get(),
                storage = get(),
                textExtractor = get(),
            )
        }
        factory { ObserveMemoryTimelineUseCase(repository = get()) }
        factory { ObserveMemoryEventsForEntityUseCase(repository = get()) }
        factory { RecordMemoryEventUseCase(repository = get()) }
        factory { DeleteMemoryEventUseCase(repository = get()) }
        factory { ObserveRelationshipsForEntityUseCase(repository = get()) }
        factory { CreateRelationshipUseCase(repository = get()) }
        factory { DeleteRelationshipUseCase(repository = get()) }
        factory { ObserveKnowledgeGraphUseCase(repository = get()) }
        factory {
            FinancialRepositories(
                accounts = get(),
                assets = get(),
                liabilities = get(),
                transactions = get(),
                categories = get(),
                budgets = get(),
                goals = get(),
            )
        }
        factory { BuildFinancialSnapshotUseCase(repositories = get()) }
        factory { RetrieveFinancialContextUseCase(searchPort = get()) }
        factory<LocalAiPort> { RuleBasedLocalAiEngine(snapshotUseCase = get(), retrieval = get()) }
        factory { AskAiAssistantUseCase(localAi = get()) }
        factory { RecordAuditEventUseCase(repository = get()) }
        factory { ObserveAuditLogUseCase(repository = get()) }
        factory { ExportBackupUseCase(backupPort = get()) }
        factory { RestoreBackupUseCase(backupPort = get()) }
        factory {
            ImportExportCoreRepositories(
                accounts = get(),
                assets = get(),
                liabilities = get(),
                categories = get(),
                budgets = get(),
                goals = get(),
                tags = get(),
            )
        }
        factory {
            ImportExportRelatedRepositories(
                transactions = get(),
                transactionTags = get(),
                documents = get(),
                memoryEvents = get(),
                relationships = get(),
                ledger = get(),
            )
        }
        factory { ExportFinancialDataUseCase(core = get(), related = get()) }
        factory { ImportFinancialDataUseCase(core = get(), related = get()) }
        factory { ExportTransactionsCsvUseCase(transactionRepository = get()) }
        factory { ImportTransactionsCsvUseCase(ledger = get()) }
    }
