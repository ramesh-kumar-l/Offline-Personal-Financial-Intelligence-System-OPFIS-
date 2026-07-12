package com.opfis.domain.ai.engine.responder

import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.AiCitation
import com.opfis.domain.ai.AiMoneyFormatter
import com.opfis.domain.ai.FinancialSnapshot
import com.opfis.domain.budget.Budget
import com.opfis.domain.entity.EntityType

/**
 * Answers BUDGET questions by listing defined limits. Spend-to-date
 * tracking against a budget's limit is a documented product gap (Phase
 * 2 only owns the budget definition, see `05-current-state.md`), so the
 * answer is explicit about not being able to say whether the user is
 * over or under budget.
 */
internal object BudgetResponder {
    fun respond(
        question: String,
        snapshot: FinancialSnapshot,
    ): AiAnswer {
        if (snapshot.budgets.isEmpty()) return AiAnswer("You have not defined any budgets yet.")
        val lower = question.lowercase()
        val matched =
            snapshot.budgets.filter { budget ->
                categoryNameOf(budget, snapshot)?.lowercase()?.let(lower::contains) ==
                    true
            }
        val relevant = matched.ifEmpty { snapshot.budgets }
        val lines =
            relevant.joinToString("; ") { budget ->
                val categoryName = categoryNameOf(budget, snapshot) ?: "Unknown category"
                "$categoryName: ${AiMoneyFormatter.format(
                    budget.limitMinorUnits,
                )} per ${budget.period.name.lowercase()}"
            }
        val text = "Defined budget limits: $lines. Spend-to-date tracking against these limits is not yet implemented."
        val citations = relevant.map { AiCitation(EntityType.BUDGET, it.id, categoryNameOf(it, snapshot) ?: it.id) }
        return AiAnswer(text, citations)
    }

    private fun categoryNameOf(
        budget: Budget,
        snapshot: FinancialSnapshot,
    ): String? = snapshot.categories.firstOrNull { it.id == budget.categoryId }?.name
}
