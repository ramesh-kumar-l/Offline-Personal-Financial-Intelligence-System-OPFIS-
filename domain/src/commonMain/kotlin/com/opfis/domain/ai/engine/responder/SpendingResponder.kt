package com.opfis.domain.ai.engine.responder

import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.AiCitation
import com.opfis.domain.ai.AiMoneyFormatter
import com.opfis.domain.ai.FinancialSnapshot
import com.opfis.domain.entity.EntityType
import com.opfis.domain.transaction.TransactionType

/** Answers SPENDING questions, narrowing to a category mentioned by name in the question when one matches. */
internal object SpendingResponder {
    fun respond(
        question: String,
        snapshot: FinancialSnapshot,
    ): AiAnswer {
        val lower = question.lowercase()
        val matchedCategory = snapshot.categories.firstOrNull { lower.contains(it.name.lowercase()) }
        val expenses = snapshot.transactions.filter { it.type == TransactionType.EXPENSE }
        val relevant =
            if (matchedCategory !=
                null
            ) {
                expenses.filter { it.categoryId == matchedCategory.id }
            } else {
                expenses
            }
        val total = relevant.sumOf { it.amountMinorUnits }
        val scopeLabel = matchedCategory?.let { "on ${it.name}" } ?: "across all categories"
        val text = "You have spent ${AiMoneyFormatter.format(total)} $scopeLabel (${relevant.size} transaction(s))."
        val citations =
            relevant
                .take(CITATION_LIMIT)
                .map { AiCitation(EntityType.TRANSACTION, it.id, it.description.ifBlank { "Transaction" }) }
        return AiAnswer(text, citations)
    }

    private const val CITATION_LIMIT = 5
}
