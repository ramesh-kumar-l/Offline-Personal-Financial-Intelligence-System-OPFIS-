# Active Initiatives

## Current phase

Phase 1 - Core Persistence (ROADMAP.md) implemented and tested; Phase 0
formally closed (build verified green on real toolchain). Awaiting
owner review/approval before Phase 2 (Financial Domain) begins - per
the phase-execution policy in SYSTEM.md, do not start Phase 2
autonomously.

## Active tasks

- Owner to review the Phase 1 implementation (encrypted persistence,
  ADR 0004/0005) and either approve moving to Phase 2 or request
  changes.
- When an Android emulator/device becomes available, add an
  instrumented test for the real `AndroidSqliteDriver` + `SupportFactory`
  encrypted path (Desktop's equivalent is already tested - see
  `05-current-state.md`).

## Blockers

None currently - the toolchain gap that blocked Phase 0 (no JDK, no
current Android SDK, no internet) was resolved this session (JDK
25.0.3, Android SDK platform 36 at `D:\Android_SDK_New`, working
internet access confirmed).
