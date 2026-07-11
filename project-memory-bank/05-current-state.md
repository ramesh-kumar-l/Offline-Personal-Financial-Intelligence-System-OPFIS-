# Current State

Last updated: 2026-07-12 (Phases 0-3 closed; Phase 4 Search implemented
and tested)

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

## Pending

- Phase 5 onward (see `04-roadmap.md` / `ROADMAP.md`): Phase 5
  "Document Intelligence" (PDF import, image import, OCR, document
  indexing, receipt vault) - in progress, see this file's next update.
