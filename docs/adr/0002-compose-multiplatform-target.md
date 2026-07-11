# ADR 0002: Compose Multiplatform as the Presentation Target

Status: Accepted
Date: 2026-07-11

## Context

`project-memory-bank/06-tech-stack.md` recorded an Android-only stack
(Jetpack Compose, Kotlin, SQLCipher, SQLite FTS5, WorkManager, ONNX
Runtime abstraction). VISION.md and PRD.md, however, describe a
"Desktop edition" as part of the long-term roadmap and require
responsive layouts across phone, tablet, desktop, and foldables
(SystemPrompt Part 3, "Responsive Design"). Phase 0 is the last point
at which choosing the presentation platform is nearly free; choosing
it later would mean rewriting the entire Presentation layer.

## Problem

Should Phase 0 scaffold an Android-only app (matching the current
tech-stack memory) or a multiplatform app that can also target desktop
without a rewrite?

## Decision

Target **Kotlin Multiplatform + Compose Multiplatform**, with
`androidTarget()` and `jvm("desktop")` registered from Phase 0 onward.
iOS/other native targets are not registered yet and are out of scope
until a future ADR requests them.

This decision was made explicitly by the project owner during Phase 0
planning (superseding the Android-only entry in `06-tech-stack.md`,
which is updated alongside this ADR).

## Consequences

- Android-only APIs (`android.security.keystore.*`, `WorkManager`,
  biometric prompts) can no longer be called directly from shared
  (`commonMain`) code. They must be defined as `expect` declarations
  in the module that needs them (starting with `:shared`'s `Logger`
  port as the first example) and implemented as `actual` per target.
- The desktop target does not have Android Keystore, WorkManager, or
  the Android biometric APIs. Phase 8 (Security) and Phase 1 (Core
  Persistence) must design their `actual` implementations with a real
  desktop equivalent in mind (e.g. OS keychain integration or a
  passphrase-derived key on desktop), not just an Android-first API
  with a desktop stub.
- Build complexity increases relative to a pure Android app: every
  module that reaches `:composeApp`'s `commonMain` must itself be a
  Kotlin Multiplatform module (see ADR 0001), not a plain JVM module.
- `06-tech-stack.md` is updated to record Compose Multiplatform,
  Kotlin Multiplatform, and the two active targets (Android, Desktop)
  in place of the Android-only description.

## Alternatives Rejected

- Stay Android-only per the existing tech-stack memory: rejected by
  the project owner because it would require a full Presentation-layer
  rewrite to add the desktop edition later, contradicting the
  Longevity pillar.

## Follow-up Actions

- When Phase 8 (Security) designs Keystore-backed encryption key
  storage, produce a desktop-equivalent `actual` in the same change,
  not as separate follow-up debt.
- Reconsider adding iOS/native targets only if a concrete product need
  arises; do not add speculatively.
