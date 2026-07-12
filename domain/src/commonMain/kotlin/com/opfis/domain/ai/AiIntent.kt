package com.opfis.domain.ai

/** The kind of financial question [RuleBasedLocalAiEngine] can specialize an answer for. */
enum class AiIntent {
    NET_WORTH,
    CASH_FLOW,
    SPENDING,
    BUDGET,
    GOAL,
    GENERAL,
}

/**
 * Keyword-based intent classification - deliberately simple and fully
 * offline (no embeddings/NLP model available in this environment, see
 * `15-ai-runtime.md`). Order matters: budget/goal are checked before the
 * broader "spending" bucket so "budget"/"goal" questions are not
 * mis-classified as generic spending questions.
 */
object AiIntentClassifier {
    private val netWorthKeywords = listOf("net worth", "how much am i worth", "total worth")
    private val cashFlowKeywords = listOf("cash flow", "income vs expense", "income and expense")
    private val budgetKeywords = listOf("budget")
    private val goalKeywords = listOf("goal", "saving for", "savings target")
    private val spendingKeywords = listOf("spend", "spent", "spending", "expense", "expenses")

    fun classify(question: String): AiIntent {
        val lower = question.lowercase()
        return when {
            netWorthKeywords.any { lower.contains(it) } -> AiIntent.NET_WORTH
            cashFlowKeywords.any { lower.contains(it) } -> AiIntent.CASH_FLOW
            budgetKeywords.any { lower.contains(it) } -> AiIntent.BUDGET
            goalKeywords.any { lower.contains(it) } -> AiIntent.GOAL
            spendingKeywords.any { lower.contains(it) } -> AiIntent.SPENDING
            else -> AiIntent.GENERAL
        }
    }
}
