# AI Runtime

## Phase 7 - Local AI (implemented)

`LocalAiPort` (`domain/.../ai/LocalAiPort.kt`) is the local-model
abstraction: `suspend fun answer(question: String, asOfEpochMillis:
Long): AiAnswer`. No model weights can be downloaded in this offline
environment, and wiring a native ONNX Runtime/llama.cpp inference
engine into a KMP Android+Desktop app is a substantial undertaking on
its own - so Phase 7 ships the port plus one deterministic, fully
offline default implementation rather than a neural model. This was an
explicit, user-confirmed scope decision (see `26-active-initiatives.md`
/ `30-session-handoff.md`); a real local LLM can later implement the
same `LocalAiPort` interface without any caller-side change.

**`RuleBasedLocalAiEngine`** (`domain/.../ai/engine/`) is the default
binding: it classifies the question's intent
(`AiIntentClassifier` - keyword-based, six buckets: `NET_WORTH`,
`CASH_FLOW`, `SPENDING`, `BUDGET`, `GOAL`, `GENERAL`), builds a
one-shot `FinancialSnapshot` via `BuildFinancialSnapshotUseCase`
(reads every relevant repository once through the bundled
`FinancialRepositories` holder, kept out of detekt's
`LongParameterList` the same way Phase 5's `ImportDocumentRequest`
was), and dispatches to a responder object under `ai/engine/responder/`:

| Intent | Responder | Reuses |
|---|---|---|
| `NET_WORTH` | `NetWorthResponder` | Phase 3's `NetWorthCalculator` |
| `CASH_FLOW` | `CashFlowResponder` | Phase 3's `CashFlowCalculator` |
| `SPENDING` | `SpendingResponder` | category-name keyword match over expense transactions |
| `BUDGET` | `BudgetResponder` | lists defined limits; explicitly states spend-to-date tracking is not implemented (documented Phase 2 gap) |
| `GOAL` | `GoalResponder` | current-vs-target progress |
| `GENERAL` | `GeneralResponder` | falls through to semantic retrieval |

Every responder returns an `AiAnswer(text, citations: List<AiCitation>)`
- `AiCitation` points back to the real `EntityType`/id/label the answer
was derived from, so the UI always shows its work (ROADMAP Phase 7,
"Explainable answers") instead of presenting an unverifiable claim.

**Semantic retrieval**: `RetrieveFinancialContextUseCase` is the
retrieval layer `GeneralResponder` falls back to. No embedding model is
available fully offline in this environment, so retrieval is lexical -
it reuses Phase 4's FTS5-backed `SearchPort` (`bm25()`-ranked) rather
than true vector similarity, mapping each `SearchResult` into a
`RetrievedItem`. This is a deliberate, documented scope cut, not an
oversight: a genuine embedding-based retriever would need a bundled
embedding model, which has the same "no downloadable weights offline"
constraint as the local LLM itself.

`AiMoneyFormatter` (internal to `domain/ai/`) intentionally duplicates
`composeApp`'s `MoneyFormatter` logic - `:domain` cannot depend on the
Presentation layer (ADR 0001), and answer text is domain logic, not UI
rendering, so a small duplication is preferable to a layering
violation.

Presentation: a 5th bottom-nav destination, "Assistant"
(`composeApp/.../ai/AiAssistantScreen` + `AiAssistantScreenBody` +
`AiCitationRow`) - a question box above a session-local conversation
history (not persisted; each `AiExchange` is presentation-only state).
See `18-ui-design-system.md`.

## Known gaps

- No real local LLM/embedding model is integrated - `LocalAiPort`'s
  only implementation is the deterministic `RuleBasedLocalAiEngine`.
  ONNX Runtime remains "planned, not yet integrated" per
  `06-tech-stack.md`.
- `AiIntentClassifier` is a fixed English keyword list - no synonym
  expansion, no multi-language support, no confidence scoring.
- `BudgetResponder` cannot say whether the user is over or under budget
  (spend-to-date tracking was never implemented - a pre-existing Phase
  2 gap, not new to Phase 7).
- `SpendingResponder`/`GoalResponder`/`BudgetResponder` category/goal
  matching is a plain substring match against the question text - no
  fuzzy matching, no handling of multiple matched categories/goals in
  one question.
- The AI conversation history is not persisted and does not itself
  become a `MemoryEvent` (Phase 6) - asking the assistant a question
  leaves no trace once the screen is left.
