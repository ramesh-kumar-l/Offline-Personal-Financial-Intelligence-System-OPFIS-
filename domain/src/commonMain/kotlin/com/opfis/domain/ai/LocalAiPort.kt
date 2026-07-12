package com.opfis.domain.ai

/**
 * Local model abstraction (ROADMAP Phase 7). The default binding
 * ([RuleBasedLocalAiEngine]) answers deterministically from the user's
 * own data - no model weights, no network call, fully offline. A real
 * local LLM (e.g. via ONNX Runtime/llama.cpp, see `06-tech-stack.md`)
 * can later implement this same port without any caller-side change.
 */
interface LocalAiPort {
    suspend fun answer(
        question: String,
        asOfEpochMillis: Long,
    ): AiAnswer
}
