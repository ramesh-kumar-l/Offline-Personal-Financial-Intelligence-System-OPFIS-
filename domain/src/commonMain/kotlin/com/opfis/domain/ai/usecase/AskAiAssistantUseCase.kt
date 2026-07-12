package com.opfis.domain.ai.usecase

import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.LocalAiPort

/** Thin Application-layer wrapper the Presentation layer calls to ask the offline AI assistant a question. */
class AskAiAssistantUseCase(
    private val localAi: LocalAiPort,
) {
    suspend operator fun invoke(
        question: String,
        asOfEpochMillis: Long,
    ): AiAnswer = localAi.answer(question, asOfEpochMillis)
}
