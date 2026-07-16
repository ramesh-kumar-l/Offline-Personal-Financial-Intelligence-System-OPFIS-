# Active Initiatives

## Current phase

Phase 9 (Import/Export) and Phase 10 (Performance) both implemented
2026-07-16, same session, per explicit owner instruction to continue
past each review checkpoint - Phases 0-8 remain closed. **Neither
phase is build-verified** - see `05-current-state.md`/
`06-tech-stack.md`/`20-performance-budget.md`. Phase 11 (Testing) is
next - not yet started, no owner instruction received to begin it.

## Active tasks

- **Blocking**: run `./gradlew ktlintCheck detekt allTests assemble`
  on a machine with a real JDK/Android SDK toolchain and fix whatever
  it surfaces - this session's environment had none (no `java`/`javac`,
  no `.gradle` cache), so Phases 9 and 10 have only been manually
  reviewed, not compiler- or test-verified. Highest-risk unknowns:
  whether `kotlinx-serialization-json` 1.8.0 resolves against Maven
  Central for Kotlin 2.4.0 (Phase 9, picked from general knowledge, not
  confirmed against real repository metadata), and whether SQLDelight's
  generated `Long` parameter type for `financial_transaction.sq`'s
  untyped `selectRecent(:limit)` bind value matches the assumption used
  in `SqlTransactionRepository.observeRecent` (Phase 10).
- **Blocking**: measure Phase 10's actual effect once the build gate is
  green - cold start, search latency, and dashboard render time against
  `20-performance-budget.md`'s targets (&lt;1s / &lt;100ms / &lt;300ms).
  No profiler is available in this environment, so Phase 10's changes
  are structurally justified but empirically unverified.
- Await owner review/direction on Phase 11 (Testing: unit, integration,
  UI, security tests, performance benchmarks) before starting - see
  `04-roadmap.md`.
- Consider enabling SQLite `WAL`/`synchronous=NORMAL` pragmas in a
  future performance pass - deliberately deferred this phase because it
  interacts with Phase 9's `restoreBackup` (which copies the main `.db`
  file but doesn't checkpoint/delete stale `-wal`/`-shm` sidecars) in a
  way that can't be verified without a real build/runtime - see
  `20-performance-budget.md`.
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
- If a future phase wants richer import/export UX, consider: restoring
  an encrypted backup without a full process kill (would need an
  in-process Koin-graph reload - out of scope for Phase 9), CSV support
  for entities beyond transactions, or surfacing `ImportSummary`'s
  counts in the audit log entry itself rather than only the UI.

## Blockers

- The build gate could not be run this session (no JDK/Android SDK) -
  see "Active tasks" above. Nothing else is blocked.
