# Current State

Last updated: 2026-07-12 (Phases 0-5 closed; Phase 6 Financial Memory
implemented and tested)

## Implemented

### Phase 0 - Foundation (closed, exit criteria met)

- Repository scaffold: 4 Gradle modules (`:shared`, `:domain`, `:data`,
  `:composeApp`) per ADR 0001, Kotlin Multiplatform + Compose
  Multiplatform per ADR 0002, Koin DI wiring per ADR 0003.
- Real toolchain verified: JDK 25.0.3, Android SDK (platform 36 / API
  36.1 extension 20, build-tools 37.0.0), Gradle 9.6.1, AGP 9.2.1 with
  compatibility flags (ADR 0004), Kotlin 2.4.0, Compose Multiplatform
  1.11.1, Koin 4.2.2.
- 5 ADRs recorded (`docs/adr/0001`-`0005`), indexed in
  `24-adr-index.md`.

### Phase 1 - Core Persistence (implemented and tested)

- Encrypted database: SQLDelight 2.3.2 + SQLCipher (ADR 0005), platform
  driver factories and key providers (expect/actual), versioned
  migrations, `PersistentSystemStatusRepository`, `BackupPort` +
  `FileBackupPort`. 7 tests, all passing (see prior session-handoff for
  detail).

### Phase 2 - Financial Domain (implemented and tested)

- Domain entities + repository ports (`:domain`, one package per
  entity): `Account`, `Asset`, `Liability`, `Category`, `Transaction`,
  `Budget`, `Goal` - see `03-domain-model.md` for fields and invariants.
  Each has `Observe`/`Upsert`/`Delete` use cases (Koin `factory` in
  `appModule`), following the Phase 0/1 `ObserveSystemStatusUseCase`
  pattern.
- Ledger engine: `TransactionLedgerRules` (pure domain policy, computes
  signed per-account balance deltas for INCOME/EXPENSE/TRANSFER and
  their reversal) + `FinancialLedgerPort`
  (`recordTransaction`/`deleteTransaction`), implemented by
  `SqlFinancialLedger` using one SQLDelight `transaction {}` block per
  operation so a transaction row and its balance adjustments are always
  atomic - see `12-financial-engine.md`.
- Schema: 7 new tables (`account`, `asset`, `liability`, `category`,
  `budget`, `goal`, `financial_transaction`), each with the mandatory
  `created_at`/`updated_at`/`version` audit columns (SYSTEM_PROMPT Part
  2). Schema bumped to v3 via `migrations/2.sqm` - see
  `11-database-schema.md`.
- Data-layer repositories: `Sql<Entity>Repository` per entity (7) +
  `SqlFinancialLedger`, all wired into `dataModule`; new use cases wired
  into `composeApp`'s `appModule`.
- Tests: 8 domain unit tests (`TransactionLedgerRulesTest` - all
  INCOME/EXPENSE/TRANSFER/reversal/validation paths;
  `CategoryTest`/`GoalTest` validation) + 21 data-layer integration
  tests against real in-memory SQLite (`SqlAccountRepositoryTest`,
  `SqlAssetRepositoryTest`, `SqlLiabilityRepositoryTest`,
  `SqlCategoryRepositoryTest`, `SqlBudgetRepositoryTest`,
  `SqlGoalRepositoryTest`, `SqlFinancialLedgerTest` - income/expense/
  transfer posting, delete-reverses-balance, unknown-id no-op) + 1 new
  schema-migration test (v2 -> v3 adds the financial tables with data
  intact). All pass; every new file is under 300 lines (largest is 145).
- Full build gate (`ktlintCheck detekt allTests assemble`, Android +
  Desktop) green: `BUILD SUCCESSFUL in 2m 3s`, 394 tasks.

### Phase 3 - Dashboard & UX (implemented and tested)

- Two new pure domain policy objects: `NetWorthCalculator` (sums
  Account + Asset, subtracts Liability, excludes archived accounts) and
  `CashFlowCalculator` (buckets non-transfer transactions into calendar
  months via `kotlinx-datetime`, correct across leap years/month-length/
  year-rollover) - see `02-system-architecture.md`, `03-domain-model.md`.
- Four new Application use cases combining existing repositories via
  `Flow.combine` (`ObserveNetWorthUseCase`, `ObserveCashFlowUseCase`,
  `ObserveRecentTransactionsUseCase`, `SearchFinancialRecordsUseCase`) -
  zero new repository methods, zero `:data`/schema changes.
- `FinancialSearchEngine`: minimal, real in-memory substring search
  across Account/Category/Transaction (see `14-search-engine.md`) -
  deliberately not the full Phase 4 FTS5 search.
- Presentation: single `DashboardScreen` (composeApp/.../dashboard/)
  with Net Worth (+ Asset Allocation donut chart), Cash Flow (+ grouped
  bar chart), Recent Activity, Search, and Trust Indicators sections.
  Charts are custom Canvas draws, no third-party charting library,
  fixed never-cycled categorical colors, icon+text+color (never color
  alone) per SystemPrompt Part 3. New `format/` package
  (`MoneyFormatter`, `MonthLabelFormatter`, `DateFormatter`) - all
  locale-API-free.
- Retired Phase 0's `SystemStatusScreen`; `App.kt` now renders
  `DashboardScreen()`. New dependency: `kotlinx-datetime` 0.6.1
  (`:domain` and `:composeApp`), justified for correct calendar-month
  math and Presentation date formatting.
- Tests: 5 new domain unit-test files (`NetWorthCalculatorTest`,
  `ObserveNetWorthUseCaseTest`, `CashFlowCalculatorTest`,
  `ObserveRecentTransactionsUseCaseTest`, `FinancialSearchEngineTest`),
  all passing. Every new/modified file is under 300 lines (largest is
  102).
- Full build gate (`ktlintCheck detekt allTests assemble`, Android +
  Desktop) green: `BUILD SUCCESSFUL in 6m 21s`, 394 tasks. (First run
  failed on 3 detekt `LongParameterList` violations - fixed by bundling
  chart-drawing geometry params into small data classes/`Rect` and
  collapsing a test helper's year/month/day into one `occurredAt`.)

### Phase 4 - Search (implemented and tested)

- Schema v4 (`migrations/3.sqm`): `tag` table, `transaction_tag` join
  table, and a `search_index` FTS5 virtual table
  (`entity_type UNINDEXED, entity_id UNINDEXED, text`) kept in sync via
  one `AFTER INSERT` (delete-then-insert) and one `AFTER DELETE` trigger
  per source table (account/category/financial_transaction/tag) - no
  `AFTER UPDATE` trigger needed since the app only ever uses
  `INSERT OR REPLACE`. Backfilled for pre-existing rows on migration.
- Domain: `Tag` entity + `TagRepository`/`TransactionTagRepository`
  ports and use cases (Observe/Upsert/Delete tag, assign/remove/observe
  transaction tags); `SearchFilter` (entity-type set, tag-id set,
  occurred-at range) and `SearchPort` (replaces `FinancialSearchEngine`);
  `SearchResult` gained `TagMatch`; `SearchFinancialRecordsUseCase`
  rewritten to fan a query `Flow<String>` and a `SearchFilter` `Flow`
  into `SearchPort.search()` via `combine`+`flatMapLatest`; new
  `TimelineEntry` + `ObserveTimelineUseCase` (chronological transaction
  browse with tag map, filterable) for the blank-query state - see
  `14-search-engine.md`.
- Data: `SqlSearchIndexRepository` runs the FTS5 `MATCH`/`bm25()`
  queries (one per entity type, unioned client-side per
  `SearchFilter.entityTypes`) - `MATCH`/`bm25()` must reference the
  virtual table's real name, never a query alias, which is now a
  documented hard requirement (SQLDelight's SQLite dialect parser
  rejects the alias form). `SqlTagRepository`,
  `SqlTransactionTagRepository`, `FtsQueryBuilder` (query-string
  escaping/prefix-matching). Extracted `AccountMapper`/`CategoryMapper`/
  `TagMapper` (top-level `internal fun toDomainX`) so both an entity's
  own repository and `SqlSearchIndexRepository` share one mapping
  function per entity.
- Presentation: new `composeApp/.../search/SearchScreen` (+
  `SearchScreenBody`, filter bar, tag filter chips, tag management,
  global-results list, timeline section with per-row tag assign/remove)
  and bottom `NavigationBar` in `App.kt` switching between
  `DashboardScreen` and `SearchScreen`. First use of
  `kotlin.uuid.Uuid.random()` for client-side id generation (creating a
  `Tag` from the UI), resolving the "no ID-generation strategy" gap.
- Tests: 3 new domain unit-test files (`TagTest`,
  `ObserveTimelineUseCaseTest`, `SearchFinancialRecordsUseCaseTest`) + 3
  new data integration-test files (`SqlTagRepositoryTest`,
  `SqlTransactionTagRepositoryTest`, `SqlSearchIndexRepositoryTest`) + a
  3rd `SchemaMigrationTest` case (v3 -> v4 adds tag/search tables and
  backfills existing rows). All pass; every new/modified file is under
  300 lines (largest: `SchemaMigrationTest.kt` at 205, after extracting
  a `createV3Schema` helper to clear a detekt `LongMethod` finding).
- Full build gate (`ktlintCheck detekt allTests assemble`) green after
  fixing: one SQLDelight FTS5 alias compile error (see above), 3 ktlint
  formatting violations, 2 detekt `LongMethod` findings (fixed by
  extracting `createV3Schema` and splitting `SearchScreen` into
  `SearchScreen`/`SearchScreenBody`).

### Phase 5 - Document Intelligence (implemented and tested)

- Domain: `Document` entity (`id, fileName, storagePath, mimeType,
  documentType, extractedText, linkedTransactionId?, importedAt,
  createdAt, updatedAt`) + `DocumentType` enum (RECEIPT/STATEMENT/
  INVOICE/OTHER). Three ports: `DocumentRepository` (observeAll,
  observeByTransaction, upsert, delete, linkToTransaction),
  `DocumentStoragePort` (save/read/delete raw bytes - the database only
  ever stores `storagePath`, never file content, to keep large binaries
  out of the SQLCipher-encrypted rows), `DocumentTextExtractorPort`
  (`extractText(bytes, mimeType): String`, returns `""` rather than
  throwing when nothing can be recovered, so import always succeeds).
  Five use cases: `ImportDocumentUseCase` (takes one
  `ImportDocumentRequest` bundling id/fileName/bytes/mimeType/
  documentType/linkedTransactionId/now - reduced from 7 loose params to
  clear a detekt `LongParameterList` finding), `ObserveDocumentsUseCase`,
  `ObserveDocumentsForTransactionUseCase`, `LinkDocumentToTransactionUseCase`,
  `DeleteDocumentUseCase`.
- Schema v5 (`migrations/4.sqm`): `document` table (`storage_path`,
  `mime_type`, `document_type`, `extracted_text` default `''`,
  `linked_transaction_id` indexed, standard audit columns) plus
  `document_search_ai`/`document_search_ad` triggers that fold
  `file_name || ' ' || extracted_text` into Phase 4's `search_index`
  FTS5 table under `entity_type = 'DOCUMENT'` - same trigger-based sync
  pattern as account/category/financial_transaction/tag, so a document
  is globally searchable the moment it's imported. `SearchResult`
  gained `DocumentMatch`.
- Data: `SqlDocumentRepository` + `DocumentMapper`. Storage and text
  extraction are platform `expect`/`actual`: `DesktopDocumentStorage`
  writes to a local app-data directory; `DesktopDocumentTextExtractor`
  reads a PDF's embedded text directly via Apache PDFBox 3.0.8, falling
  back to Tesseract OCR (`tess4j` 5.19.0, via a small `TesseractEngine`
  wrapper) for image files or PDFs with no embedded text.
  `AndroidDocumentStorage` writes to app-private storage;
  `AndroidDocumentTextExtractor` renders PDF pages to bitmaps via
  `PdfRenderer` (Android has no bundled PDF-text API) and OCRs every
  page with ML Kit's standalone on-device text recognizer
  (`com.google.mlkit:text-recognition` 16.0.1 - model bundled in the
  app, no Play Services network call, fully offline) - not verified on
  a device/emulator in this environment (no Android runtime available
  here), same caveat as the existing SQLCipher Android path.
- Presentation: new `composeApp/.../document/` package - "Receipt
  Vault" is a new bottom-nav destination (`AppDestination.Vault`,
  alongside Dashboard and Search) rendering `DocumentVaultScreen` (state/
  wiring) + `DocumentVaultScreenBody` (layout: import button, document
  list, each row optionally linkable to a transaction and deletable).
  `DocumentPicker` is `expect`/`actual`
  (`rememberDocumentPickerLauncher`): Desktop uses `java.awt.FileDialog`
  (constructed with an explicit `Frame?` cast - passing bare `null`
  is an overload-resolution ambiguity between the `Frame`- and
  `Dialog`-parent constructor overloads); Android uses
  `ActivityResultContracts.GetContent()` + `ContentResolver` to read
  bytes/MIME type/display name from the returned `Uri` - not verified
  on a device/emulator in this environment, same caveat as the OCR path
  above.
- Tests: `DocumentTest` (entity validation), `ImportDocumentUseCaseTest`
  (fakes for all three ports), `SqlDocumentRepositoryTest`,
  `DesktopDocumentTextExtractorTest` (real PDFBox/Tesseract against
  fixture files). The existing `SchemaMigrationTest`'s v1-to-current
  full-chain case implicitly exercises the v4->v5 `document` migration
  (schema creation fails loudly on driver open if it were broken), but
  there is no dedicated v4->v5 assertion case matching the v2/v3
  pattern - a minor coverage gap, not a correctness gap. All new files
  are under 300 lines (largest: 96, `DesktopDocumentTextExtractorTest.kt`).
- Full build gate (`ktlintCheck detekt allTests assemble`, Android +
  Desktop) green: `BUILD SUCCESSFUL in 2m 56s`, 395 tasks. Took 4
  attempts to go fully green - see `30-session-handoff.md` for the full
  list of issues hit and fixed (2 ktlint, 3 detekt `LongParameterList`/
  `LongMethod`/`DestructuringDeclarationWithTooManyEntries`/
  `NestedBlockDepth`, plus 3 genuine compile errors: a `FileDialog(null,
  ...)` constructor ambiguity, a suspend-function-inside-`joinToString`
  violation, and a spurious `import
  androidx.compose.foundation.layout.weight` that shadowed an internal
  Compose property).

### Phase 6 - Financial Memory (implemented and tested)

- Domain: `MemoryEvent` (NOTE/MILESTONE, optional `subject: EntityRef`)
  + `MemoryEventRepository` + 4 use cases; `Relationship` (typed link
  between two `EntityRef`s) + `RelationshipRepository` + 3 CRUD use
  cases; a pure `KnowledgeGraphBuilder` projecting a root entity's
  relationships into a 1-hop `KnowledgeGraph`
  (`ObserveKnowledgeGraphUseCase`) - deliberately not a full transitive
  traversal across every entity's existing foreign keys. New
  cross-cutting `domain/entity/` package (`EntityType`, `EntityRef`),
  kept separate from Phase 4's `SearchEntityType`. See
  `13-memory-engine.md` for the full design rationale, including the
  deliberate decision not to auto-generate `MemoryEvent`s from other
  use cases this phase.
- Schema v6 (`migrations/5.sqm`): `memory_event` and `relationship`
  tables. `memory_event` is wired into the FTS5 `search_index` (same
  trigger pattern as document/tag/account/category/transaction);
  `SearchResult` gained `MemoryEventMatch`. `relationship` is not
  search-indexed (no free text).
- Data: `SqlMemoryEventRepository` + `MemoryEventMapper`,
  `SqlRelationshipRepository` + `RelationshipMapper`
  (`data/.../memory/`, `data/.../relationship/`).
  `SqlSearchIndexRepository` extended with `searchMemoryEvents`,
  following the exact same per-entity-type pattern as Document (Phase
  5). One real bug hit and fixed: SQLDelight generates row classes for
  multi-word snake_case tables by capitalizing only the first letter
  and keeping underscores (`Memory_event`, matching the existing
  `Financial_transaction`/`Transaction_tag` precedent) rather than
  full PascalCase - `MemoryEventMapper.kt` initially assumed the wrong
  generated name.
- Presentation: a 4th bottom-nav destination, "Memory"
  (`composeApp/.../memory/MemoryScreen` + `MemoryScreenBody` +
  `MemoryEventRow`) - an inline note/milestone recording form above the
  chronological timeline. No dedicated UI for `Relationship`/
  `KnowledgeGraph` yet (engine only) - see `13-memory-engine.md`.
- Tests: `MemoryEventTest`, `RelationshipTest`, `KnowledgeGraphBuilderTest`
  (domain unit tests, including one exercising both relationship
  directions), `ObserveKnowledgeGraphUseCaseTest` (fake repository),
  `SqlMemoryEventRepositoryTest`, `SqlRelationshipRepositoryTest` (real
  in-memory SQLite). The existing v1-to-current `SchemaMigrationTest`
  case was extended with an assertion that `memory_event` is queryable
  post-migration, proving the full v1->v6 migration chain (including
  Phase 5's 4.sqm and this phase's 5.sqm) applies cleanly - this also
  retroactively closes the "no dedicated v4->v5 test" gap noted after
  Phase 5. All new files are under 300 lines (largest: 228,
  `SchemaMigrationTest.kt`, which predates this phase).
- Full build gate (`ktlintCheck detekt allTests assemble`, Android +
  Desktop) green: `BUILD SUCCESSFUL in 5m 12s`, 395 tasks. Took 3
  attempts: (1) the `Memory_event` naming bug above plus a ktlint
  chain-method-continuation violation and an unused import; (2) a
  non-exhaustive `when` in `SearchResultRow.kt` missing the new
  `SearchResult.MemoryEventMatch` branch. See `30-session-handoff.md`
  for full detail.

## Known gaps / not yet verified

- Android's encrypted driver path has no instrumented test (no
  emulator/device in this environment) - unchanged from Phase 1.
- `androidx.security.crypto` deprecation and Desktop key-file hardening
  remain explicit Phase 8 follow-ups (ADR 0005) - unchanged.
- AGP 9's compatibility flags (ADR 0004) remain a deprecated-but-working
  path with a shelf life before AGP 10.0 - unchanged.
- No ID-generation utility exists yet for new Phase 2 entities (use
  cases accept fully-formed entities with caller-supplied ids) -
  expected to land with Phase 3 UI.
- Budget spend-vs-limit analytics remain unimplemented (net worth is
  now covered by Phase 3; budget analytics was never in Phase 3's
  ROADMAP scope either - revisit if a future phase calls for it).
- No multi-currency/FX support; all monetary fields assume one implicit
  currency system-wide.
- No SQLite `PRAGMA foreign_keys` enforcement; cross-entity references
  (`category_id`, `account_id`, `transfer_account_id`, `parent_id`) are
  plain columns, not declared FK constraints.
- No historical net-worth snapshots exist, so Phase 3 deliberately has
  no "net worth trend" chart (would require fabricating data).
- `dataviz` skill's `validate_palette.js` could not run (no `node` in
  this environment) - chart palette relies on the project's existing
  icon+text+color convention instead of a formal CVD validation run.
- Phase 4's `SearchFilter.tagIds`/date-range fields deliberately apply
  only to the Timeline browse, not the FTS5 global-text-search port -
  text relevance vs. browsing are treated as separate concerns by
  design, not an oversight.
- Phase 5's OCR/PDF-extraction paths (Tesseract on Desktop, ML Kit on
  Android) and both `DocumentPicker` actuals are unverified against real
  files/devices beyond the fixture-based `DesktopDocumentTextExtractorTest`
  - no Android runtime available in this environment, same recurring
  caveat as the SQLCipher Android path.
- Phase 6's `MemoryEvent`s are manually recorded only - no other use
  case in the app auto-generates one, and `MemoryScreen`'s form never
  sets `subject` - see `13-memory-engine.md` for the scope rationale.
- Phase 6's `Relationship`/`KnowledgeGraph` engine (domain + data +
  tests) has no presentation layer yet - only `MemoryEvent` got a
  screen this phase.

## Pending

- Phase 7 onward (see `04-roadmap.md` / `ROADMAP.md`): Phase 7 "Local
  AI" (local model abstraction, AI assistant, explainable answers,
  semantic retrieval) - not yet started.
