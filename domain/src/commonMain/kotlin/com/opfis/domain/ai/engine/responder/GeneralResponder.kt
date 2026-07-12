package com.opfis.domain.ai.engine.responder

import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.AiCitation
import com.opfis.domain.ai.usecase.RetrieveFinancialContextUseCase

/** Fallback for GENERAL questions: falls through to lexical retrieval over every searchable record. */
internal object GeneralResponder {
    suspend fun respond(
        question: String,
        retrieval: RetrieveFinancialContextUseCase,
    ): AiAnswer {
        val items = retrieval(question)
        if (items.isEmpty()) {
            return AiAnswer("I could not find anything in your financial data matching \"$question\".")
        }
        val text = "I found ${items.size} related record(s): " + items.joinToString("; ") { it.summary }
        val citations = items.map { AiCitation(it.entityType, it.entityId, it.summary) }
        return AiAnswer(text, citations)
    }
}
