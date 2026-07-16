package com.opfis.domain.importexport.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountRepository
import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetRepository
import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentRepository
import com.opfis.domain.entity.EntityType
import com.opfis.domain.goal.Goal
import com.opfis.domain.goal.GoalRepository
import com.opfis.domain.importexport.ImportExportCoreRepositories
import com.opfis.domain.importexport.ImportExportRelatedRepositories
import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventRepository
import com.opfis.domain.relationship.Relationship
import com.opfis.domain.relationship.RelationshipRepository
import com.opfis.domain.tag.Tag
import com.opfis.domain.tag.TagRepository
import com.opfis.domain.tag.TransactionTagRepository
import com.opfis.domain.transaction.FinancialLedgerPort
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Shared fakes for [ImportExportUseCasesTest] - one mutable in-memory list per repository. */
internal class FakeAccountRepository : AccountRepository {
    val items = mutableListOf<Account>()

    override fun observeAll(): Flow<List<Account>> = flowOf(items.toList())

    override fun observeById(id: String) = flowOf(items.find { it.id == id })

    override suspend fun upsert(account: Account) {
        items.add(account)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeAssetRepository : AssetRepository {
    val items = mutableListOf<Asset>()

    override fun observeAll(): Flow<List<Asset>> = flowOf(items.toList())

    override suspend fun upsert(asset: Asset) {
        items.add(asset)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeLiabilityRepository : LiabilityRepository {
    val items = mutableListOf<Liability>()

    override fun observeAll(): Flow<List<Liability>> = flowOf(items.toList())

    override suspend fun upsert(liability: Liability) {
        items.add(liability)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeCategoryRepository : CategoryRepository {
    val items = mutableListOf<Category>()

    override fun observeAll(): Flow<List<Category>> = flowOf(items.toList())

    override suspend fun upsert(category: Category) {
        items.add(category)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeBudgetRepository : BudgetRepository {
    val items = mutableListOf<Budget>()

    override fun observeAll(): Flow<List<Budget>> = flowOf(items.toList())

    override suspend fun upsert(budget: Budget) {
        items.add(budget)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeGoalRepository : GoalRepository {
    val items = mutableListOf<Goal>()

    override fun observeAll(): Flow<List<Goal>> = flowOf(items.toList())

    override suspend fun upsert(goal: Goal) {
        items.add(goal)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeTagRepository : TagRepository {
    val items = mutableListOf<Tag>()

    override fun observeAll(): Flow<List<Tag>> = flowOf(items.toList())

    override suspend fun upsert(tag: Tag) {
        items.add(tag)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeTransactionRepository : TransactionRepository {
    val items = mutableListOf<Transaction>()

    override fun observeAll(): Flow<List<Transaction>> = flowOf(items.toList())

    override fun observeByAccount(accountId: String) = flowOf(items.filter { it.accountId == accountId })

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        flowOf(items.sortedByDescending { it.occurredAt }.take(limit))
}

internal class FakeTransactionTagRepository : TransactionTagRepository {
    var tagIdsByTransaction: Map<String, List<String>> = emptyMap()
    val assigned = mutableListOf<Pair<String, String>>()

    override fun observeTagIdsByTransaction(): Flow<Map<String, List<String>>> = flowOf(tagIdsByTransaction)

    override suspend fun assignTag(
        transactionId: String,
        tagId: String,
    ) {
        assigned.add(transactionId to tagId)
    }

    override suspend fun unassignTag(
        transactionId: String,
        tagId: String,
    ) = error("not used in this test")
}

internal class FakeDocumentRepository : DocumentRepository {
    val items = mutableListOf<Document>()

    override fun observeAll(): Flow<List<Document>> = flowOf(items.toList())

    override fun observeByTransaction(transactionId: String) =
        flowOf(items.filter { it.linkedTransactionId == transactionId })

    override suspend fun upsert(document: Document) {
        items.add(document)
    }

    override suspend fun delete(id: String) = error("not used in this test")

    override suspend fun linkToTransaction(
        documentId: String,
        transactionId: String?,
    ) = error("not used in this test")
}

internal class FakeMemoryEventRepository : MemoryEventRepository {
    val items = mutableListOf<MemoryEvent>()

    override fun observeAll(): Flow<List<MemoryEvent>> = flowOf(items.toList())

    override fun observeBySubject(
        entityType: EntityType,
        entityId: String,
    ) = flowOf(emptyList<MemoryEvent>())

    override suspend fun upsert(event: MemoryEvent) {
        items.add(event)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeRelationshipRepository : RelationshipRepository {
    val items = mutableListOf<Relationship>()

    override fun observeAll(): Flow<List<Relationship>> = flowOf(items.toList())

    override fun observeInvolving(
        entityType: EntityType,
        entityId: String,
    ) = flowOf(emptyList<Relationship>())

    override suspend fun upsert(relationship: Relationship) {
        items.add(relationship)
    }

    override suspend fun delete(id: String) = error("not used in this test")
}

internal class FakeFinancialLedgerPort : FinancialLedgerPort {
    val recorded = mutableListOf<Transaction>()

    override suspend fun recordTransaction(transaction: Transaction) {
        recorded.add(transaction)
    }

    override suspend fun deleteTransaction(transactionId: String) = error("not used in this test")
}

internal class ImportExportFixture {
    val accounts = FakeAccountRepository()
    val assets = FakeAssetRepository()
    val liabilities = FakeLiabilityRepository()
    val categories = FakeCategoryRepository()
    val budgets = FakeBudgetRepository()
    val goals = FakeGoalRepository()
    val tags = FakeTagRepository()
    val transactions = FakeTransactionRepository()
    val transactionTags = FakeTransactionTagRepository()
    val documents = FakeDocumentRepository()
    val memoryEvents = FakeMemoryEventRepository()
    val relationships = FakeRelationshipRepository()
    val ledger = FakeFinancialLedgerPort()

    val core = ImportExportCoreRepositories(accounts, assets, liabilities, categories, budgets, goals, tags)
    val related =
        ImportExportRelatedRepositories(transactions, transactionTags, documents, memoryEvents, relationships, ledger)
}
