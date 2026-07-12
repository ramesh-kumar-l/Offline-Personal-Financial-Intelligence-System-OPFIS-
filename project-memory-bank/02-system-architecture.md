# System Architecture

Clean Architecture + DDD.
Layers: Presentation, Application, Domain, Infrastructure.

## Module graph (Phase 0, see ADR 0001)

```
composeApp (Presentation, composition root)
  -> domain (Domain + Application, package-separated)
  -> data   (Infrastructure)
  -> shared (kernel)

data   -> domain, shared
domain -> shared
shared -> (nothing)
```

`:domain` holds both the Domain and Application layers in one Gradle
module for now, separated by package (`com.opfis.domain.<feature>` for
entities/ports, `com.opfis.domain.<feature>.usecase` for use cases).
It depends only on `:shared` and takes zero framework dependencies
(no Koin, no Compose, no SQL). `:shared` is a zero-dependency kernel
for cross-cutting abstractions (e.g. the `Logger` port) - not a place
for business logic.

Koin (DI) is used only in `:data` (repository bindings) and
`:composeApp` (composition root / use case wiring) - see ADR 0003.
`:domain` and `:shared` are Koin-free and testable with plain
constructor injection.

Platform target: Kotlin Multiplatform + Compose Multiplatform,
Android + Desktop (JVM) - see ADR 0002. Android-only APIs are behind
`expect`/`actual`; the first example is `Logger` in `:shared`
(`shared/src/androidMain`, `shared/src/desktopMain`).

## Persistence (Phase 1, see ADR 0005)

`:data` owns the encrypted database (SQLDelight + SQLCipher):
`DatabaseDriverFactory` and `DatabaseKeyProvider` are `expect`/`actual`
(same pattern as `Logger`); `PersistentSystemStatusRepository`
implements `:domain`'s `SystemStatusRepository` against it.
`BackupPort` is a `:domain` port (pure Kotlin, no SQL/file APIs in its
signature) implemented by `:data`'s `FileBackupPort`. Composition root
wiring is split: `dataModule` (commonMain) has Logger and repository
bindings; `androidDataModule`/`desktopDataModule` (platform-specific,
loaded from `OpfisApplication.kt`/`Main.kt`) provide the driver
factory, key provider, `SqlDriver`, `OpfisDatabase`, and `BackupPort`,
since those need a platform `Context` or directory that commonMain
doesn't have.

## Financial domain (Phase 2, see `03-domain-model.md` / `12-financial-engine.md`)

Account/Asset/Liability/Category/Budget/Goal follow the same
entity + repository-port + use-case pattern as `systemstatus`, one
package per entity under `:domain`, implemented by `Sql<Entity>Repository`
in `:data`. Transactions split reads from writes: `TransactionRepository`
(read-only) vs. `FinancialLedgerPort` (posts/reverses a transaction and
its account-balance deltas as one atomic SQLDelight `transaction {}`
block, implemented by `SqlFinancialLedger`). The balance-delta sign
convention itself lives in `TransactionLedgerRules`, a pure domain
policy object with no SQL/framework dependency, so it is unit-tested
without a database.

## Dashboard & UX (Phase 3, see `05-current-state.md`)

Read-only phase - no `:data` or schema changes. Two new pure domain
policy objects follow the `TransactionLedgerRules` pattern:
`NetWorthCalculator` (sums Account + Asset, subtracts Liability,
excludes archived accounts) and `CashFlowCalculator` (buckets
non-transfer transactions into calendar months using `kotlinx-datetime`
for correct leap-year/month-length/year-rollover math, with pure
hand-rolled `floorDiv`/`floorMod` to stay JVM-API-free in `commonMain`).
Four new Application-layer use cases in `:domain` (`ObserveNetWorthUseCase`,
`ObserveCashFlowUseCase`, `ObserveRecentTransactionsUseCase`,
`SearchFinancialRecordsUseCase`) each use `kotlinx.coroutines.flow.combine`
to fan-in 2-3 repository `Flow`s (plus a query `Flow` for search) into one
derived stream - no new repository methods were needed. `FinancialSearchEngine`
is a deliberately minimal in-memory substring search (see
`14-search-engine.md`); Phase 4 replaces it with FTS5.

Presentation: `composeApp/.../dashboard/` holds one `DashboardScreen`
assembling five Card sections (Net Worth, Cash Flow, Recent Activity,
Search, Trust Indicators). Two Canvas-based custom charts
(`dashboard/chart/`) - an Asset Allocation donut and a Cash Flow grouped
bar chart - use fixed, never-cycled categorical colors and pair every
colored element with an icon/marker + text label (SystemPrompt Part 3,
"never color alone"). No third-party charting library was added. The
old `SystemStatusScreen` (Phase 0) is retired; its trust-indicator
concept lives on as `TrustIndicatorsSection` inside the new dashboard.

## Search (Phase 4, see `14-search-engine.md`)

Adds `Tag`/`TransactionTagRepository` (`:domain`) following the
existing entity+port+use-case pattern, plus a cross-cutting
`SearchPort` implemented by `:data`'s `SqlSearchIndexRepository`
against a SQLite FTS5 virtual table (`search_index`) kept in sync via
`AFTER INSERT`/`AFTER DELETE` triggers on `account`/`category`/
`financial_transaction`/`tag`. `SearchFinancialRecordsUseCase` replaces
Phase 3's in-memory `FinancialSearchEngine`. A new `ObserveTimelineUseCase`
combines `TransactionRepository` and the tag assignment map for a
chronological, filterable, taggable browse view (the blank-query state
of the new `SearchScreen`). `composeApp/.../search/` adds bottom
`NavigationBar`-driven navigation between `DashboardScreen` and
`SearchScreen`. First use of `kotlin.uuid.Uuid` for client-generated
entity ids (creating a `Tag` from the UI).

## Document Intelligence (Phase 5, see `16-document-engine.md`)

Adds `Document` (`:domain`) following the existing entity+port+use-case
pattern, plus two new platform-scoped ports beyond the usual
`DocumentRepository`: `DocumentStoragePort` (owns raw file bytes on
disk - the database only ever stores a `storagePath`, never the file
content) and `DocumentTextExtractorPort` (PDF-text/OCR extraction,
never throws - returns `""` so import always succeeds even when no
text can be recovered). Both are `expect`/`actual`-backed per platform
in `:data`: Desktop reads a digital PDF's embedded text via Apache
PDFBox, falling back to Tesseract OCR (`tess4j`) for images or scanned
PDFs; Android has no bundled PDF-text API, so every PDF page is
rendered to a bitmap via `PdfRenderer` and OCR'd with ML Kit's
on-device text recognizer (fully offline, no Play Services network
call). Schema v5 (`migrations/4.sqm`) adds a `document` table wired
into Phase 4's `search_index` FTS5 table via the same trigger-based
sync pattern, so imported documents are globally searchable
immediately (`SearchResult` gained `DocumentMatch`). Presentation adds
a third bottom-nav destination, "Vault" (`DocumentVaultScreen` +
`DocumentVaultScreenBody`), with a `DocumentPicker` `expect`/`actual`
(`java.awt.FileDialog` on Desktop, `ActivityResultContracts.GetContent()`
on Android) launching the OS file picker.

## Financial Memory (Phase 6, see `13-memory-engine.md`)

Two new `:domain` packages follow the existing entity+port+use-case
pattern: `memory/` (`MemoryEvent`, manually recorded NOTE/MILESTONE
entries) and `relationship/` (`Relationship`, a typed user-declared
link between two entities, plus a pure `KnowledgeGraphBuilder`
projecting a root entity's relationships into a 1-hop read model - not
a full transitive graph traversal). Both reference entities generically
via a new cross-cutting `domain/entity/` package (`EntityType`,
`EntityRef`), kept deliberately separate from Phase 4's
`SearchEntityType`. Schema v6 adds `memory_event` (wired into the FTS5
`search_index`, same trigger pattern as every other content-bearing
table) and `relationship` (not search-indexed - no free text).
Presentation adds a 4th bottom-nav destination, "Memory"
(`MemoryScreen` + `MemoryScreenBody` + `MemoryEventRow`); the
`Relationship`/`KnowledgeGraph` engine has no UI yet, by design (exit
criteria was "financial memory engine," not a full UI).

## Local AI (Phase 7, see `15-ai-runtime.md`)

Read-only phase - no `:data` or schema changes, same pattern as Phase
3. `LocalAiPort` (`:domain`) is the local-model abstraction; its only
binding, `RuleBasedLocalAiEngine`, is a deterministic rule engine, not
a neural model - no model weights can be downloaded in this offline
sandboxed environment, and this was an explicit, user-confirmed scope
decision rather than an oversight. The engine classifies question
intent (`AiIntentClassifier`), builds a one-shot `FinancialSnapshot`
from existing repositories, and dispatches to one of six responder
objects, each reusing an existing Phase 2/3/4 calculator/port
(`NetWorthCalculator`, `CashFlowCalculator`, `SearchPort`) rather than
re-deriving logic. `RetrieveFinancialContextUseCase` is the semantic-
retrieval layer - lexical (FTS5/`bm25()`) rather than vector-embedding
based, for the same "no downloadable model" reason. Presentation adds a
5th bottom-nav destination, "Assistant" (`AiAssistantScreen` +
`AiAssistantScreenBody` + `AiCitationRow`), a question box above a
session-local (not persisted) conversation history.
