# Active Initiatives

## Current phase

Phase 11 (Testing) implemented 2026-07-17. Phases 0-11 are now closed
and **build-verified**: `./gradlew ktlintCheck detekt allTests
assemble` is green for both Android and Desktop, using a real JDK 21 +
Android SDK 36 toolchain the owner installed this session. This
resolved the standing blocker from the Phase 9/10 session (no
toolchain was available then) and, along the way, found and fixed
several real bugs the manual-review-only process had missed - see
`05-current-state.md`'s Phase 11 section and `19-testing-strategy.md`.
Phase 12 (MVP Release) is next - not yet started, no owner instruction
received to begin it.

## Active tasks

- Manually time cold start, search latency, and dashboard render
  against `20-performance-budget.md`'s targets (&lt;1s / &lt;100ms /
  &lt;300ms) on a real device/desktop - no automated benchmark harness
  exists, so this is the one part of Phase 10's exit criterion still
  unconfirmed even though the build itself is now green.
- Expand `:composeApp` UI test coverage beyond `LockScreenBody` (the
  only screen tested this phase) to the other ~9 screens - most
  already follow the `XScreen`/`XScreenBody` split, so the
  `XScreenBody` composables (pure layout, no Koin injection) are the
  natural next candidates, same pattern as this phase's test.
- Consider adding Android instrumented tests when an
  emulator/device becomes available in this environment - the
  encrypted driver path, biometric auth, OCR, and
  `DocumentPicker`/`FileSaver` Android actuals remain unverified on a
  real device (recurring gap since Phase 1).
- Consider enabling SQLite `WAL`/`synchronous=NORMAL` pragmas in a
  future performance pass - still deliberately deferred, same
  restore-corruption risk noted in `20-performance-budget.md`.
- Full OS keychain/DPAPI integration for Desktop's database key is
  still open - Phase 8 only added owner-only file permissions/ACL
  hardening, not the ADR 0005-flagged full integration - see
  `09-security-model.md`.
- Consider a real local LLM/embedding model binding for `LocalAiPort`
  if/when one becomes feasible in this environment (bundled weights,
  ONNX Runtime/llama.cpp integration) - `RuleBasedLocalAiEngine` is a
  deliberate, user-confirmed placeholder, not the end state - see
  `15-ai-runtime.md`.
- Build a presentation layer for Phase 6's `Relationship`/
  `KnowledgeGraph` engine (domain + data + tests are complete; only the
  UI was deferred - see `13-memory-engine.md`).
- Consider whether/which existing use cases should auto-generate
  `MemoryEvent`s (e.g. "goal reached", "budget exceeded") - deliberately
  out of scope so far; revisit once there's a clearer picture of which
  events are actually worth surfacing.
- When `node`/npm becomes available in this environment, run the
  `dataviz` skill's `scripts/validate_palette.js` against the dashboard
  chart palette to formally confirm CVD-safety (carried over from
  Phase 3 - not blocking, current mitigation is icon+text+color).
- If a future phase wants richer import/export UX, consider: restoring
  an encrypted backup without a full process kill (would need an
  in-process Koin-graph reload), CSV support for entities beyond
  transactions, or surfacing `ImportSummary`'s counts in the audit log
  entry itself rather than only the UI.
- Phase 12 (MVP Release: documentation, demo, release notes, packaging,
  v1.0) - not yet started, awaiting owner review per ROADMAP.md's "stop
  for review before the next phase" policy.

## Blockers

None currently. The JDK/Android SDK toolchain blocker that affected
Phases 9-10 is resolved as of this session.
