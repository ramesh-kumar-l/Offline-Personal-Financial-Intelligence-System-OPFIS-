# Changelog

All notable changes to OPFIS are documented in this file. Entries are
grouped by release, per `ROADMAP.md`'s phase structure.

## [1.0.0] - 2026-07-17 - MVP Release (Phase 12)

First public MVP. Everything below was built offline-first, on-device,
with no server component and no telemetry.

### Added

- **Foundation** - Clean Architecture across four Gradle modules
  (`:shared`, `:domain`, `:data`, `:composeApp`), Kotlin Multiplatform
  + Compose Multiplatform targeting Android and Desktop, Koin
  dependency injection.
- **Core persistence** - SQLDelight + SQLCipher encrypted local
  database, versioned schema migrations, encrypted backup/restore
  primitives.
- **Financial domain** - Accounts, assets, liabilities, categories,
  budgets, goals, and a transaction ledger with atomic balance
  posting/reversal for income, expense, and transfer entries.
- **Dashboard** - Net worth, cash flow (with custom asset-allocation
  and cash-flow charts), recent activity, and trust indicators.
- **Search** - SQLite FTS5 full-text search across every entity type,
  tag-based filtering, and a chronological timeline browse.
- **Document intelligence** - Receipt/statement/invoice import (PDF,
  image) with local OCR/text extraction (PDFBox + Tesseract on
  Desktop, PdfRenderer + ML Kit on Android), searchable and linkable
  to transactions.
- **Financial memory** - A hand-recorded note/milestone timeline, plus
  a typed relationship + knowledge-graph engine (domain/data complete;
  UI not yet built).
- **Local AI assistant** - Rule-based, fully offline question
  answering over the user's own data, with every answer citing the
  specific accounts/transactions/documents it was derived from.
- **Security** - Biometric unlock (Android) with a manual-confirm
  fallback (Desktop), 5-minute idle auto-lock, and an append-only
  audit log of unlocks and data operations.
- **Import/export** - Full-dataset JSON export/import, transactions-
  only CSV export/import, and encrypted whole-database backup/restore.
- **Performance** - Indexed/bounded recent-transaction queries,
  additional schema indexes, background database pre-warming at
  startup, and a reduced auto-lock polling interval.
- **Testing** - Unit tests for every domain use case, data-layer
  integration tests against real in-memory SQLite, the first
  Compose UI test (`LockScreenBody`), and a full green build gate
  (`ktlintCheck`, `detekt`, `allTests`, `assemble`) across Android and
  Desktop.
- **Packaging** - Desktop native distribution configuration
  (`compose.desktop.application.nativeDistributions`: MSI/DMG/DEB via
  `jpackage`), version bumped to `1.0.0`.

### Known limitations (see `project-memory-bank/05-current-state.md`)

- No real neural/local-LLM model is bundled; the AI assistant is a
  deterministic rule engine by deliberate scope decision.
- Android's encrypted-storage, biometric, OCR, and file-picker code
  paths have not been exercised on a real device or emulator in this
  development environment.
- Performance budgets (`project-memory-bank/20-performance-budget.md`)
  are structurally targeted but not yet empirically measured - no
  profiler/benchmark harness exists.
- The `Relationship`/knowledge-graph engine has no dedicated UI yet.
- UI test coverage is one screen (`LockScreenBody`) of roughly ten.

---

## [0.1.0] - Phase 0 - Foundation

Initial repository scaffold: module boundaries, DI wiring, coding
standards, CI, and the first 5 ADRs. No financial domain logic yet.
