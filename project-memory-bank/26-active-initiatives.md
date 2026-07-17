# Active Initiatives

## Current phase

**Phase 12 (MVP Release) implemented 2026-07-18 - v1.0.0 shipped.**
ROADMAP.md's full phase list (0-12) is now closed and build-verified:
`./gradlew ktlintCheck detekt allTests assemble` is green for both
Android and Desktop, `README.md`/`CHANGELOG.md`/`DEMO.md` document the
real v1.0.0 feature set, and a real (unsigned) `OPFIS-1.0.0.msi`
installer was built and verified against the actual toolchain - see
`05-current-state.md`'s Phase 12 section and `25-release-checklist.md`
for the full sign-off. ROADMAP.md defines no Phase 13; everything
below is forward-looking work, not a next scheduled phase.

## Active tasks

- Provision a Desktop code-signing certificate and an Android
  release-signing keystore, then produce signed distributable
  artifacts - the v1.0.0 MSI built this phase is unsigned and no
  Android release artifact was produced, both deliberately (no keys
  exist in this environment) - see `25-release-checklist.md`.
- Create and push the `v1.0.0` git tag once the owner confirms - left
  as an explicit owner action, not done automatically.
- Build-verify macOS DMG / Linux DEB packaging on a machine with that
  OS - this development environment is Windows-only, so only the MSI
  target format has actually been produced.
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
## Blockers

None currently. The JDK/Android SDK toolchain blocker that affected
Phases 9-10 is resolved as of this session. Signed-artifact
distribution is blocked on the owner provisioning a code-signing
certificate and Android keystore - not a technical blocker, an
awaited-credential one.
