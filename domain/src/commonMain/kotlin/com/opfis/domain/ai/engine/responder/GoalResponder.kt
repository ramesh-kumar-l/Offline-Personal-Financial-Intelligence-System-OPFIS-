package com.opfis.domain.ai.engine.responder

import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.AiCitation
import com.opfis.domain.ai.AiMoneyFormatter
import com.opfis.domain.ai.FinancialSnapshot
import com.opfis.domain.entity.EntityType

/** Answers GOAL questions, narrowing to a goal mentioned by name in the question when one matches. */
internal object GoalResponder {
    fun respond(
        question: String,
        snapshot: FinancialSnapshot,
    ): AiAnswer {
        if (snapshot.goals.isEmpty()) return AiAnswer("You have not defined any goals yet.")
        val lower = question.lowercase()
        val matched = snapshot.goals.filter { lower.contains(it.name.lowercase()) }
        val relevant = matched.ifEmpty { snapshot.goals }
        val lines =
            relevant.joinToString("; ") { goal ->
                "${goal.name}: ${AiMoneyFormatter.format(goal.currentAmountMinorUnits)} of " +
                    AiMoneyFormatter.format(goal.targetAmountMinorUnits)
            }
        val citations = relevant.map { AiCitation(EntityType.GOAL, it.id, it.name) }
        return AiAnswer("Goal progress: $lines.", citations)
    }
}
