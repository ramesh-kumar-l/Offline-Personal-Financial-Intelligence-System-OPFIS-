# Architecture Document

## System Overview

OPFIS is a Kotlin Multiplatform application (Android + Desktop/JVM,
Compose Multiplatform for UI) built as a strict, single-process, local
Clean Architecture system. There is no server, no network layer, and
no client-server boundary anywhere in the design — the only I/O
boundary that matters is the one between the app and its own local,
SQLCipher-encrypted SQLite database.

Four Gradle modules enforce a one-directional dependency graph, and
that direction is enforced by the build system itself (a Presentation
file physically cannot import a SQL type — it isn't on the classpath),
not by code-review convention (ADR 0001).

## Architecture Diagram (description)

```
                 +----------------------------+
                 |        :composeApp         |   Presentation + composition root
                 | (Android + Desktop targets)|   Koin startKoin() x2 entry points
                 +---------------+------------+
                                 | depends on
                 +---------------v------------+
                 |           :data            |   Infrastructure
                 | Repository impls, Koin      |   Platform SQL drivers,
                 | bindings, platform actuals   |   OCR, biometric, backup
                 +---------------+------------+
                                 | depends on
                 +---------------v------------+
                 |          :domain           |   Domain + Application
                 | Entities, repository ports, |   Zero framework deps
                 | use cases                   |   (no Koin, Compose, SQL)
                 +---------------+------------+
                                 | depends on
                 +---------------v------------+
                 |          :shared            |   Kernel
                 | Cross-cutting ports          |   Zero dependencies
                 | (e.g. Logger)                |
                 +------------------------------+
```

Every screen in `:composeApp` observes domain state via Kotlin
`Flow`s sourced from `:data`'s SQLDelight-backed repositories — there
is no "refresh" action anywhere in the UI; every screen is always
live against the encrypted database.

## Core Components

- **7 screens** (`:composeApp`): Dashboard, Search (+ Timeline),
  Vault (documents/OCR), Memory (notes/milestones), Assistant
  (rule-based Q&A), Security (biometric/audit log), Data
  (import/export/backup). Each follows an `XScreen` /
  `XScreenBody` split: `XScreen` does Koin injection and state
  hookup, `XScreenBody` is a pure, injection-free composable —
  the pattern that makes UI testing possible without a DI container.
- **Domain layer** (`:domain`): entities (`Account`, `Asset`,
  `Liability`, `Transaction`, `Category`, `Budget`, `Goal`, `Tag`,
  `Document`, `MemoryEvent`, `Relationship`, …), repository interfaces
  (ports), and use cases — e.g. `FinancialLedgerPort` is the single
  place transactions are recorded, keeping account balances
  consistent by construction rather than by convention at every call
  site.
- **Infrastructure layer** (`:data`): `Sql*Repository` implementations
  over SQLDelight-generated typed queries, platform-specific
  `DatabaseDriverFactory`/`DatabaseKeyProvider` (`expect`/`actual`),
  OCR adapters, biometric adapters, backup port implementations.
- **AI runtime**: `LocalAiPort` is implemented today by
  `RuleBasedLocalAiEngine` — deterministic, cites its source rows, zero
  network calls, and upgradeable to a real local model later without
  changing the contract the UI depends on.
- **Search**: SQLite FTS5 virtual tables indexed alongside the primary
  schema, giving instant full-text search with no separate search
  service.
- **Security**: SQLCipher encryption at rest on both platforms
  (different key-management strategy per platform, see Tradeoffs
  below), biometric/manual auto-lock, and an append-only audit log
  table recording every unlock and data operation.

## Tradeoffs

Drawn directly from the project's 5 ratified ADRs — real tradeoffs
made and recorded, not idealized after the fact:

- **Module granularity vs. build-graph enforcement** (ADR 0001): chose
  a lean 4-module split over either 11+ pre-created empty modules
  (premature structure) or a single package-layered module (boundary
  not enforceable by the compiler). Accepted cost: `:domain` currently
  does double duty as both Domain and Application layers, to be
  revisited if it grows unwieldy.
- **Multiplatform reach vs. build complexity** (ADR 0002): chose
  Kotlin Multiplatform + Compose Multiplatform (Android + Desktop)
  over an Android-only app, accepting that every Android-only API
  (Keystore, biometric prompts) now needs an `expect`/`actual` seam.
  Rejected staying Android-only because it would require a full
  Presentation-layer rewrite later to add Desktop.
- **DI simplicity vs. platform reach** (ADR 0003): chose Koin,
  restricted to the composition root only, over Hilt (Android-only,
  incompatible with the Desktop target) and over pure manual factories
  (judged unwieldy once the composition root grows past Phase 2).
  `:domain`/`:shared` stay framework-free by rule, testable with plain
  constructor injection.
- **Toolchain currency vs. structural churn** (ADR 0004): when AGP 9
  broke the KMP + `com.android.application` plugin combination
  mid-project, chose the documented compatibility-flag bypass
  (`android.builtInKotlin=false`, `android.newDsl=false`) over an
  immediate structural migration to `com.android.kotlin.multiplatform.library`
  — explicitly time-boxed ("this ADR's decision has a shelf life,"
  revisit before AGP 10) rather than silently deferred.
- **Encryption uniformity vs. real platform constraints** (ADR 0005):
  chose SQLDelight + SQLCipher on both platforms, but with materially
  different key-management strength per platform — Android's key is
  Keystore-backed (`EncryptedSharedPreferences`), Desktop's is a
  randomly generated file-based key, explicitly documented as "the
  acknowledged weak point of this ADR," with OS keychain/DPAPI
  integration tracked as real follow-up work, not silently ignored.

## Scaling Strategy

OPFIS is single-user, single-device by design — "scaling" here means
something different from a multi-tenant server system:

- **Dataset size**: schema indexes and bounded queries (Phase 10) keep
  dashboard/recent-activity queries fast as a single user's data grows
  across years, not across users.
- **Search index growth**: SQLite FTS5 scales with the same dataset,
  no separate search infrastructure to provision.
- **Multi-device sync**: an explicit non-goal for v1.0 (VISION.md §9:
  "force online synchronization" is listed as something OPFIS will
  not do) — any future sync would need to be user-controlled
  infrastructure, not an OPFIS-hosted service, to stay consistent with
  the trust model.

## Reliability Strategy

- SQLCipher-encrypted storage with a versioned SQLDelight migration
  chain, proven end-to-end by `SchemaMigrationTest`.
- Whole-database encrypted backup/restore, with restore requiring an
  explicit user confirmation (destructive) and a full app restart
  (the running process cannot safely hot-swap its database driver —
  a deliberate, documented constraint, not an oversight).
- An append-only audit log gives a tamper-evident record of every
  unlock and data operation, independent of the primary financial
  data tables.
- Every `Sql*Repository` has an integration test against a real
  in-memory SQLite instance (not mocked), and every domain use case
  has unit test coverage against hand-written fakes — see
  `documents/05-benchmark-report.md` for what is and isn't empirically
  verified beyond correctness (performance is not yet measured).
