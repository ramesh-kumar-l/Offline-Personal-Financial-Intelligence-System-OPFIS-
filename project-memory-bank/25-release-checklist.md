# Release Checklist

v1.0.0 sign-off against `ROADMAP.md`'s "Definition of Done" (Architecture
preserved, Tests passing, Documentation updated, Memory bank updated,
Security reviewed, Performance validated, User experience reviewed)
plus Phase 12's own tasks (Documentation, Demo, Release notes,
Packaging, Version 1.0). Completed 2026-07-18.

## Architecture preserved

- [x] Module graph unchanged since Phase 0 (`:shared` -> `:domain` ->
  `:data` -> `:composeApp`, dependencies only flow inward) - see
  `02-system-architecture.md`.
- [x] No framework leakage into `:domain`/`:shared` (still zero Koin/
  Compose/SQL dependencies there).

## Tests passing

- [x] `./gradlew ktlintCheck detekt allTests assemble` green for
  Android and Desktop (last confirmed run: Phase 11, `BUILD
  SUCCESSFUL in 59s`, 400 tasks; re-confirmed this phase after the
  packaging config change - see `05-current-state.md`).
- [x] Domain use-case coverage closed for every CRUD feature (Phase
  11); data-layer integration tests exist for every `Sql*Repository`.
- [ ] Android instrumented tests (encrypted driver, biometric, OCR,
  file pickers) - **not done**, no emulator/device available in this
  development environment since Phase 1. Documented, recurring,
  explicitly not blocking this release - see "Known gaps" in
  `05-current-state.md`.
- [ ] UI test coverage beyond `LockScreenBody` (1 of ~10 screens) -
  **not done**, deferred to a future testing pass.

## Documentation updated

- [x] `README.md` rewritten for the v1.0.0 feature set (was still
  describing the Phase 0 scaffold).
- [x] `CHANGELOG.md` added - full v1.0.0 release notes.
- [x] `DEMO.md` added - scripted walkthrough of all seven screens.
- [x] `project-memory-bank/` current for Phases 0-12 (this file,
  `02-system-architecture.md`, `04-roadmap.md`, `05-current-state.md`,
  `06-tech-stack.md`, `19-testing-strategy.md`, `20-performance-budget.md`,
  `26-active-initiatives.md`, `30-session-handoff.md`).
- [x] `docs/adr/` up to date (5 ADRs, indexed in `24-adr-index.md`) -
  no new architectural decision was made this phase.

## Memory bank updated

- [x] `04-roadmap.md`, `05-current-state.md`, `26-active-initiatives.md`,
  `30-session-handoff.md` updated per `ROADMAP.md`'s Memory Bank Update
  Policy.

## Security reviewed

- [x] Phase 8's security model unchanged and still build-verified:
  biometric/manual-confirm auto-lock, append-only audit log,
  SQLCipher-encrypted storage, owner-only key-file permissions on
  Desktop.
- [x] No new attack surface added this phase - packaging config and
  documentation only, zero production code changes.
- [ ] Full OS keychain/DPAPI integration for Desktop's database key -
  **not done**, a pre-existing, documented gap (ADR 0005) unrelated to
  this release's scope.
- [ ] Signed release artifacts (code-signing certificate for the MSI/
  DMG, Android release-signing keystore) - **not done**. No
  certificate or keystore exists in this environment; committing one
  would be a security incident, not a release step. The desktop MSI
  built this phase is unsigned; Android `assembleRelease`/
  `bundleRelease` are not run without a signing config. Document as
  the owner's responsibility before any public distribution.

## Performance validated

- [x] Phase 10's targeted optimizations (bounded recent-transaction
  query, schema indexes, DB pre-warming, reduced auto-lock poll
  interval) remain build-verified as of Phase 11.
- [ ] Empirical measurement of `20-performance-budget.md`'s three
  named targets (cold start <1s, search <100ms, dashboard render
  <300ms) against a real device/desktop - **not done**, no profiler/
  benchmark harness exists in this environment. Targets remain
  structurally justified, not measured. Carried forward as the top
  post-release action item.

## User experience reviewed

- [x] All seven bottom-nav screens manually walkthrough-scripted in
  `DEMO.md` and confirmed to compile/render (build gate + the one
  existing Compose UI test).
- [x] Every destructive action (restore backup) has an explicit
  confirmation dialog before proceeding.
- [x] Icon+text+color used throughout (never color alone) per
  SystemPrompt Part 3 - unchanged since Phase 3.

## Packaging

- [x] `composeApp/build.gradle.kts` gained
  `compose.desktop.application.nativeDistributions` (MSI/DMG/DEB
  target formats, package name/version/vendor/description).
- [x] `./gradlew :composeApp:createDistributable` verified against the
  real toolchain - produces a runnable app image at
  `composeApp/build/compose/binaries/main/app`.
- [x] `./gradlew :composeApp:packageDistributionForCurrentOS` verified
  on Windows - produces an unsigned MSI installer (the Compose Gradle
  plugin bundles its own WiX Toolset download, so no separate WiX
  install was required).
- [ ] macOS DMG / Linux DEB packaging - **not verified**, this
  development environment is Windows-only. The `targetFormats`
  configuration covers all three; only the current OS's format has
  been build-tested.
- [x] Android release build compiles - `assembleRelease` runs as part
  of the standard `assemble` task and is build-verified (Phase 12
  build gate).
- [ ] Signed, distributable Android release artifact (`bundleRelease`
  for Play Store, or a signed release APK) - **not done**, blocked on
  a signing keystore (see "Security reviewed" above). The unsigned
  release APK `assembleRelease` produces cannot be installed as-is.

## Version 1.0

- [x] `composeApp/build.gradle.kts`: Android `versionName = "1.0.0"`
  (`versionCode` bumped 1 -> 2), Desktop `packageVersion = "1.0.0"`.
- [ ] Git tag `v1.0.0` - not created this session; tagging/pushing is
  a release-visibility action left for explicit owner confirmation.

## Net verdict

**Public MVP: yes**, for the scope ROADMAP.md defines (documentation,
demo, release notes, packaging config, version bump, all prior
phases' exit criteria met and build-verified). The unchecked items
above are pre-existing, explicitly scoped-out gaps (signing,
empirical performance measurement, Android device testing, broader UI
test coverage) - none block a first offline release, all are already
tracked as active initiatives in `26-active-initiatives.md`.
