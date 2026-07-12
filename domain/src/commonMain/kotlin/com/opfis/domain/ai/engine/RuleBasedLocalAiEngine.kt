package com.opfis.domain.ai.engine

import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.AiIntent
import com.opfis.domain.ai.AiIntentClassifier
import com.opfis.domain.ai.LocalAiPort
import com.opfis.domain.ai.engine.responder.BudgetResponder
import com.opfis.domain.ai.engine.responder.CashFlowResponder
import com.opfis.domain.ai.engine.responder.GeneralResponder
import com.opfis.domain.ai.engine.responder.GoalResponder
import com.opfis.domain.ai.engine.responder.NetWorthResponder
import com.opfis.domain.ai.engine.responder.SpendingResponder
import com.opfis.domain.ai.usecase.BuildFinancialSnapshotUseCase
import com.opfis.domain.ai.usecase.RetrieveFinancialContextUseCase

/**
 * Default [LocalAiPort] implementation (ROADMAP Phase 7, "Local model
 * abstraction"): a deterministic, fully-offline rule engine, not a
 * neural model - no model weights can be downloaded in this offline
 * environment (see `15-ai-runtime.md`). It classifies the question's
 * intent, builds a fresh [com.opfis.domain.ai.FinancialSnapshot], and
 * dispatches to the matching responder, each of which cites the real
 * records it derived the answer from.
 */
class RuleBasedLocalAiEngine(
    private val snapshotUseCase: BuildFinancialSnapshotUseCase,
    private val retrieval: RetrieveFinancialContextUseCase,
) : LocalAiPort {
    override suspend fun answer(
        question: String,
        asOfEpochMillis: Long,
    ): AiAnswer {
        val snapshot = snapshotUseCase()
        return when (AiIntentClassifier.classify(question)) {
            AiIntent.NET_WORTH -> NetWorthResponder.respond(snapshot)
            AiIntent.CASH_FLOW -> CashFlowResponder.respond(snapshot, asOfEpochMillis)
            AiIntent.SPENDING -> SpendingResponder.respond(question, snapshot)
            AiIntent.BUDGET -> BudgetResponder.respond(question, snapshot)
            AiIntent.GOAL -> GoalResponder.respond(question, snapshot)
            AiIntent.GENERAL -> GeneralResponder.respond(question, retrieval)
        }
    }
}
