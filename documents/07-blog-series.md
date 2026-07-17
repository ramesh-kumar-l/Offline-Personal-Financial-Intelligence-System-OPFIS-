# Blog Series Outline

Seven articles, titles + summaries + publishing order only (not full
drafts — each is a future writing task on its own). Ordered for
compounding distribution: start with the most shareable/curiosity-
driven piece, end with the most durable reference piece.

## Publishing Order

### 1. "I built a financial app that has never called an API"
**Summary**: The hook article. Leads with the differentiator — a
personal finance app whose AI assistant, search, and every screen work
with the network disabled, by architecture, not as a feature flag.
Ends with a link to `DEMO.md` so readers can verify the claim
themselves. Best "Show HN" candidate of the series.

### 2. "Explainable AI without an LLM"
**Summary**: How `RuleBasedLocalAiEngine` answers financial questions
and cites the exact rows behind every answer, why that beat waiting
for a bundled local model, and the `LocalAiPort` abstraction that
lets a real model replace it later without touching the UI. Directly
useful to anyone building AI features under a privacy or
offline constraint.

### 3. "Four modules, one dependency direction: Clean Architecture in Kotlin Multiplatform"
**Summary**: Walks through ADR 0001's reasoning — why not 11 modules,
why not one — and shows concretely how the Gradle module graph, not
code review, prevents a Presentation-layer file from importing SQL
directly. Reusable template for any KMP project.

### 4. "The build broke on day one of a real toolchain: an AGP 9 post-mortem"
**Summary**: A war-story format around ADR 0004 — the exact error
message, the vendor migration doc that explained it, the decision to
take the documented compatibility-flag bypass over a larger structural
migration, and why that decision was deliberately given an expiration
date. Strong technical-credibility piece; doubles as case-study
supporting material.

### 5. "Encrypting a database the same way on two platforms that don't agree on what 'secure key storage' means"
**Summary**: ADR 0005's Android-vs-Desktop key management asymmetry,
told honestly — Android gets real Keystore-backed protection, Desktop
gets a documented weak point. Why shipping the honest asymmetry beat
either blocking the release on parity or hiding the gap.

### 6. "A memory bank for AI pairing: how a 30-file directory kept a solo project coherent across sessions"
**Summary**: The `project-memory-bank/` convention itself as the
subject — numbered topic files, a defined update policy, and how it
functions as a compressed save-state for both a human returning to the
project and an AI collaborator starting cold. The most directly
reusable pattern in the whole series, independent of OPFIS's domain.

### 7. "Shipping v1.0 of a 12-phase project, solo, and writing down what's still not done"
**Summary**: Closes the series. How the release checklist
(`25-release-checklist.md`) format — every item explicitly checked or
unchecked with a one-line reason — produced a more trustworthy release
than a checklist with only the passing items shown. Reflective,
process-focused piece; good LinkedIn-length companion post exists as a
condensed version of this one.
