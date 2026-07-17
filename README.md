# OPFIS - Offline Personal Financial Intelligence System

**Your financial life, remembered - entirely offline.**

![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)
![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg)
![Compose Multiplatform](https://img.shields.io/badge/UI-Compose%20Multiplatform-4285F4.svg)

Privacy-first, offline-first, AI-native personal financial
intelligence platform. Every screen runs against a single
SQLCipher-encrypted local database, and its AI assistant cites the
exact accounts/transactions/documents behind every answer - no server,
no cloud AI call, no account required. See [VISION.md](VISION.md),
[PRD.md](PRD.md), and [ROADMAP.md](ROADMAP.md) for the product
definition, [DEMO.md](DEMO.md) for a guided walkthrough,
[CHANGELOG.md](CHANGELOG.md) for release history, and
[SYSTEM.md](SYSTEM.md) plus [SystemPrompt/](SystemPrompt/) for the
engineering operating system this repository is built under.

## Status

**v1.0.0 - MVP Release.** All 12 roadmap phases (0-11: Foundation,
Persistence, Financial Domain, Dashboard, Search, Documents, Memory,
Local AI, Security, Import/Export, Performance, Testing) are
implemented and build-verified: `./gradlew ktlintCheck detekt allTests
assemble` is green for both Android and Desktop. See
`project-memory-bank/05-current-state.md` for the detailed,
up-to-date status and `project-memory-bank/25-release-checklist.md`
for the release sign-off.

## What it does

OPFIS is a local-only personal finance app - no server, no account,
no cloud sync. Everything lives in one SQLCipher-encrypted SQLite
database on the user's own device. Seven screens, all reachable from
the bottom navigation bar:

- **Dashboard** - net worth, cash flow, recent activity, trust
  indicators, and a search entry point.
- **Search** - instant offline full-text search (SQLite FTS5) across
  accounts, categories, transactions, tags, documents, and memory
  events, plus a filterable chronological timeline.
- **Vault** - import receipts/statements/invoices (PDF or image); text
  is extracted locally (PDFBox + Tesseract OCR on Desktop, PdfRenderer
  + ML Kit on Android) and becomes searchable, with optional linking to
  a transaction.
- **Memory** - a hand-recorded financial timeline (notes and
  milestones); the underlying `Relationship`/knowledge-graph engine
  exists but has no dedicated screen yet.
- **Assistant** - a local, rule-based question-answering engine that
  cites the accounts/transactions/documents behind every answer. No
  data ever leaves the device; there is no cloud LLM call.
- **Security** - biometric unlock (Android) / manual confirm
  (Desktop), a 5-minute auto-lock, and an append-only audit log of
  every unlock and data export/import/restore.
- **Data** - JSON export/import of the full dataset, CSV export/import
  of transactions, and encrypted whole-database backup/restore.

## Architecture

Clean Architecture + DDD, four Gradle modules:

- `:shared` - zero-dependency kernel (e.g. the `Logger` port).
- `:domain` - Domain + Application layers (entities, repository
  interfaces, use cases). No framework or platform dependencies.
- `:data` - Infrastructure layer (repository implementations, Koin
  bindings).
- `:composeApp` - Presentation layer and composition root. Kotlin
  Multiplatform (Compose Multiplatform), targeting Android and
  Desktop.

See `docs/adr/` for the reasoning behind these decisions and
`project-memory-bank/02-system-architecture.md` for how each phase
extended this structure.

## Toolchain setup

You need:

1. **JDK 21+** (this project is verified against JDK 21.0.11).
2. **Android SDK** with `platform;android-36` and a recent
   `build-tools` version, plus `cmdline-tools` (for `sdkmanager`).
   Point `ANDROID_HOME`/`ANDROID_SDK_ROOT` at it, or create
   `local.properties` at the repo root with:
   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   ```

Then verify:

```sh
./gradlew ktlintCheck detekt allTests assemble
```

## Running it

```sh
./gradlew :composeApp:run                 # Desktop, runs in place
./gradlew :composeApp:assembleDebug       # Android debug APK
```

## Packaging a release build

Desktop native distributions (app image + installer) are configured
via the Compose Multiplatform Gradle plugin:

```sh
./gradlew :composeApp:createDistributable        # runnable app image, no installer tooling needed
./gradlew :composeApp:packageDistributionForCurrentOS  # installer for the current OS
```

`packageDistributionForCurrentOS` produces an MSI on Windows (requires
the WiX Toolset v3 to be installed), a DMG on macOS, or a DEB on
Linux - all via `jpackage`, bundled with the JDK. Android release
builds (`assembleRelease`/`bundleRelease`) need a signing config,
which is intentionally not committed to this repository (see
`project-memory-bank/25-release-checklist.md`).

## Documentation

- [QuickStarterGuide.md](QuickStarterGuide.md) - zero to a running
  build in a few minutes (the action-only path).
- [DEMO.md](DEMO.md) - full scripted walkthrough of every screen.
- [`documents/`](documents/) - engineering thesis, architecture
  document, ADR collection, benchmark report, and other deeper-dive
  material.
- [`project-memory-bank/`](project-memory-bank/) - the engineering
  memory bank; start with `05-current-state.md`.

## Repository layout

```
composeApp/   Presentation layer + composition root (Android, Desktop)
domain/       Domain + Application layers (pure Kotlin)
data/         Infrastructure layer
shared/       Cross-cutting kernel (no business logic)
docs/adr/     Architecture Decision Records
project-memory-bank/   Engineering memory bank (read this first)
```

## License

MIT - see [LICENSE](LICENSE).

## Contributing

This is currently a single-maintainer project. Issues and PRs are
welcome; there is no formal contribution process yet.
