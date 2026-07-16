package com.opfis.domain.importexport

import com.opfis.domain.account.Account
import com.opfis.domain.asset.Asset
import com.opfis.domain.budget.Budget
import com.opfis.domain.category.Category
import com.opfis.domain.document.Document
import com.opfis.domain.goal.Goal
import com.opfis.domain.liability.Liability
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.relationship.Relationship
import com.opfis.domain.tag.Tag
import com.opfis.domain.transaction.Transaction
import kotlinx.serialization.Serializable

/** One row of the `transaction_tag` join table - no domain entity exists for it, see [TransactionTagRepository]. */
@Serializable
data class TransactionTagAssignment(
    val transactionId: String,
    val tagId: String,
)

/**
 * A full, portable snapshot of every user-owned financial entity
 * (ROADMAP Phase 9, "JSON" export/import) - everything except the
 * append-only [com.opfis.domain.audit.AuditLogEntry] audit trail, which
 * isn't "financial data" to round-trip. [Document] rows carry only
 * metadata/extracted text, never raw file bytes (see
 * [com.opfis.domain.document.DocumentStoragePort]) - restoring a
 * snapshot recreates document records, not the files they point to.
 */
@Serializable
data class FinancialDataSnapshot(
    val schemaVersion: Int = 1,
    val exportedAtEpochMillis: Long,
    val accounts: List<Account>,
    val assets: List<Asset>,
    val liabilities: List<Liability>,
    val categories: List<Category>,
    val transactions: List<Transaction>,
    val budgets: List<Budget>,
    val goals: List<Goal>,
    val tags: List<Tag>,
    val transactionTagAssignments: List<TransactionTagAssignment>,
    val documents: List<Document>,
    val memoryEvents: List<MemoryEvent>,
    val relationships: List<Relationship>,
)
