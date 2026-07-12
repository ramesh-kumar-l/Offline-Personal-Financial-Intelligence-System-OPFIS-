# Active Initiatives

## Current phase

Phase 6 - Financial Memory (ROADMAP.md) implemented and tested; Phases
0-5 remain closed. Phase 7 (Local AI: local model abstraction, AI
assistant, explainable answers, semantic retrieval) is next - not yet
started, no owner instruction received to begin it.

## Active tasks

- Await owner review/direction on Phase 7 (Local AI) before starting -
  see `04-roadmap.md`.
- Build a presentation layer for Phase 6's `Relationship`/
  `KnowledgeGraph` engine (domain + data + tests are complete; only the
  UI was deferred - see `13-memory-engine.md`).
- Consider whether/which existing use cases should auto-generate
  `MemoryEvent`s (e.g. "goal reached", "budget exceeded") - deliberately
  out of scope for Phase 6's first pass; revisit once there's a clearer
  picture of which events are actually worth surfacing.
- When an Android emulator/device becomes available, add an
  instrumented test for the real `AndroidSqliteDriver` + `SupportFactory`
  encrypted path (carried over from Phase 1 - still open) and verify
  Phase 5's OCR (ML Kit) + `DocumentPicker` Android paths.
- When `node`/npm becomes available in this environment, run the
  `dataviz` skill's `scripts/validate_palette.js` against the dashboard
  chart palette to formally confirm CVD-safety (carried over from
  Phase 3 - not blocking, current mitigation is icon+text+color).

## Blockers

None currently.
