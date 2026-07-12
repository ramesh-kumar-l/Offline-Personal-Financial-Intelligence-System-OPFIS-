# Session Handoff

Last session: 2026-07-12

## Completed in Phase 6 session (Financial Memory)

- Implemented Phase 6 per ROADMAP.md: `MemoryEvent`/`MemoryEventRepository`
  + 4 use cases, `Relationship`/`RelationshipRepository` + 3 CRUD use
  cases, a pure `KnowledgeGraphBuilder` (+ `ObserveKnowledgeGraphUseCase`)
  projecting 1-hop neighbors from a root entity's relationships, and a
  new cross-cutting `domain/entity/` package (`EntityType`, `EntityRef`).
  Schema v6 (`migrations/5.sqm`) adds `memory_event` (wired into the
  FTS5 `search_index`) and `relationship` (not search-indexed). New
  `composeApp/.../memory/` package (`MemoryScreen` + `MemoryScreenBody`
  + `MemoryEventRow`) as a 4th bottom-nav destination, "Memory". Full
  detail in `05-current-state.md` and `13-memory-engine.md`.
- Scope decisions made explicit (see `13-memory-engine.md`,
  `26-active-initiatives.md`): `MemoryEvent`s are manually recorded
  only (no other use case auto-emits one); `Relationship`/`KnowledgeGraph`
  got a complete, tested engine but no browsing UI this phase; the
  `MemoryScreen` form never sets `MemoryEvent.subject`.
- The build gate took 3 attempts to go green. Issues hit and fixed, in
  order: (1) `MemoryEventMapper.kt` assumed SQLDelight would generate
  `com.opfis.data.db.MemoryEvent` for the `memory_event` table, but
  SQLDelight names row classes for multi-word snake_case tables by
  capitalizing only the first letter and keeping underscores
  (`Memory_event`) - confirmed against the existing
  `Financial_transaction`/`Transaction_tag` precedent already in this
  codebase, which I should have checked before assuming PascalCase from
  the single-word `Document`/`Tag` tables. Also fixed in the same pass:
  a ktlint `chain-method-continuation` violation in `SchemaMigrationTest.kt`
  and an unused `androidx.compose.ui.unit.dp` import in `MemoryEventRow.kt`.
  (2) A non-exhaustive `when` in `SearchResultRow.kt` - adding
  `SearchResult.MemoryEventMatch` to the sealed class without updating
  every existing `when (result)` over it is a recurring failure mode in
  this codebase (the compiler catches it, but only when that specific
  file recompiles - worth double-checking sealed-class call sites
  immediately after adding a new subtype, not waiting for the gate to
  catch it). (3) `BUILD SUCCESSFUL in 5m 12s`, 395 tasks - confirmed via
  the literal log text.
- Every new/modified file is under 300 lines (largest pre-existing file
  touched: `SchemaMigrationTest.kt` at 232, `SqlSearchIndexRepository.kt`
  at 131 after adding the 6th search source).
- Updated memory bank: `02-system-architecture.md`, `03-domain-model.md`,
  `04-roadmap.md`, `05-current-state.md`, `07-repository-structure.md`,
  `11-database-schema.md`, `13-memory-engine.md` (fleshed out from a
  2-line stub), `14-search-engine.md`, `18-ui-design-system.md`,
  `26-active-initiatives.md`, this file.

## Not completed (as of end of Phase 6 session)

- Same Phase 1 items as before (Android instrumented test,
  `androidx.security.crypto` deprecation, Desktop key-file hardening) -
  all explicit Phase 8 scope, unchanged.
- Formal CVD palette validation (blocked on `node` availability) -
  unchanged since Phase 3.
- Phase 5's OCR (Tesseract/ML Kit) and `DocumentPicker` actuals are
  still unverified against real files/an Android device/emulator.
- Phase 6's `Relationship`/`KnowledgeGraph` engine has no presentation
  layer; `MemoryEvent`s are manually recorded only (no auto-generation
  from other features).
- Phase 7 (Local AI: local model abstraction, AI assistant, explainable
  answers, semantic retrieval) - not started; no owner instruction
  received to begin it yet.

## Next recommended task (as of end of Phase 6 session)

1. Owner review of Phase 6 and explicit direction on Phase 7 (Local AI)
   - the standing ROADMAP.md policy is to stop for review between
     phases; this session proceeded directly from Phase 5 into Phase 6
     only because the owner explicitly authorized it in the same
     message that closed out Phase 5.
2. When an Android emulator/device is available, add the instrumented
   test flagged since Phase 1 and verify Phase 5's Android OCR/picker
   paths.
3. When `node`/npm becomes available, run `validate_palette.js` against
   the dashboard's chart colors.
4. If Phase 7 needs it, revisit whether `MemoryEvent` auto-generation
   or a `Relationship`/`KnowledgeGraph` browsing UI should be built
   first (both deferred from Phase 6).

## Completed in Phase 5 session (Document Intelligence)

- Implemented Phase 5 per ROADMAP.md: `Document` entity/ports/5 use
  cases (`:domain`), `SqlDocumentRepository` + platform storage/OCR
  actuals (`:data` - PDFBox+Tesseract on Desktop, `PdfRenderer`+ML Kit
  on Android), schema v5 (`migrations/4.sqm`, `document` table wired
  into the Phase 4 FTS5 `search_index`), and a new "Vault"
  bottom-nav destination (`DocumentVaultScreen` + `DocumentPicker`
  `expect`/`actual`) - full detail in `05-current-state.md` and
  `16-document-engine.md`.
- The build gate took 4 attempts to go green. Issues hit and fixed, in
  order: (1) 2 ktlint violations (a >120-char line in
  `SearchResultRow.kt`, an import-order swap in
  `DesktopDocumentTextExtractorTest.kt`) + 3 detekt findings
  (`LongParameterList` on `ImportDocumentUseCase` - fixed via an
  `ImportDocumentRequest` bundling data class; `LongMethod` on a
  pre-existing `SchemaMigrationTest.createV3Schema` - fixed by
  extracting `createV3Tables`/`seedV3Data`; `LongParameterList` on
  `DocumentVaultScreenBody` at exactly 6 params - confirmed detekt's
  `functionThreshold` fires *at* the threshold, not only above it,
  fixed via a `DocumentVaultActions` bundling data class) - discovered
  a pre-existing 12-param `LongParameterList` on `SearchScreenBody`
  from Phase 4 in the same pass, fixed via `SearchScreenState`/
  `SearchScreenActions`. (2) Second run surfaced 3 genuine compile
  errors + 1 new detekt finding: `FileDialog(null, "Import Document",
  FileDialog.LOAD)` in `DocumentPicker.desktop.kt` is an
  overload-resolution ambiguity between the `Frame`- and
  `Dialog`-parent 3-arg constructors (`java.awt.FileDialog` has both) -
  fixed with an explicit `null as Frame?` cast; a suspend function
  (`recognizeText`) called inside `joinToString`'s `transform` lambda
  in `AndroidDocumentTextExtractor.extractFromPdf` - `joinToString`'s
  lambda type is a plain (non-suspend) function type so this doesn't
  compile even though `joinToString` itself is inline - fixed by
  collecting into a `mutableListOf` via a plain `for` loop, then
  joining; a `DestructuringDeclarationWithTooManyEntries` detekt
  finding introduced by my own earlier `SearchScreenState` fix
  (destructuring 5 fields, detekt's default max is 3) - fixed by
  reverting to plain `state.field` property access. (3) Third run: the
  `for`-loop fix for the suspend/joinToString bug added a nesting level
  that tripped detekt's `NestedBlockDepth` - fixed by extracting
  `recognizeAllPages` as its own method; a `TagManagementSection.kt`
  compile error ("Cannot access 'val RowColumnParentData?.weight:
  Float': it is internal in file") traced to a spurious
  `import androidx.compose.foundation.layout.weight` - `Modifier.weight`
  inside a `Row`/`Column` scope needs no import at all (it's a member
  extension on the implicit `RowScope`/`ColumnScope` receiver); the
  import instead resolved to an unrelated *internal* top-level property
  of the same name inside Compose's own `RowColumn.kt` - fixed by
  deleting the import (confirmed via another working file in this repo
  that uses `.weight()` with no such import). (4) Fourth run: `BUILD
  SUCCESSFUL in 2m 56s`, 395 tasks - genuinely green, confirmed via the
  literal log text (task-notifications falsely reported "exit code 0"/
  "completed" on at least 2 of the 3 failing runs - never trust that
  signal over the log's own `BUILD SUCCESSFUL`/`BUILD FAILED` text).
- Every new/modified file is under 300 lines (largest: 96,
  `DesktopDocumentTextExtractorTest.kt`).
- Updated memory bank: `02-system-architecture.md`,
  `03-domain-model.md`, `04-roadmap.md`, `05-current-state.md`,
  `07-repository-structure.md`, `11-database-schema.md`,
  `14-search-engine.md`, `16-document-engine.md`,
  `18-ui-design-system.md`, `26-active-initiatives.md`, this file.

## Not completed (as of end of Phase 5 session)

- Same Phase 1 items as before (Android instrumented test,
  `androidx.security.crypto` deprecation, Desktop key-file hardening) -
  all explicit Phase 8 scope, unchanged.
- Formal CVD palette validation (blocked on `node` availability) -
  unchanged since Phase 3.
- Phase 5's OCR (Tesseract/ML Kit) and `DocumentPicker` actuals are
  unverified against real files/an Android device/emulator.
- No dedicated `SchemaMigrationTest` case for v4->v5 (`document`
  table) - see `05-current-state.md`.
- Phase 6 (Financial Memory: Timeline, Memory events, Relationships,
  Knowledge graph abstractions) - starting this session per explicit
  owner instruction (no separate approval gate requested).

## Next recommended task (as of end of Phase 5 session)

1. Implement Phase 6 (Financial Memory) - see `13-memory-engine.md`
   once populated.
2. When an Android emulator/device is available, add the instrumented
   test flagged since Phase 1 and verify Phase 5's Android OCR/picker
   paths.
3. When `node`/npm becomes available, run `validate_palette.js` against
   the dashboard's chart colors.
4. Add the missing v4->v5 `SchemaMigrationTest` case.

## Completed in Phase 4 session (Search)

- Implemented Phase 4 per ROADMAP.md: SQLite FTS5 (`search_index`
  virtual table + sync triggers), global search (`SearchPort`/
  `SqlSearchIndexRepository`), filters (`SearchFilter`), timeline search
  (`TimelineEntry`/`ObserveTimelineUseCase`), tags (`Tag`,
  `TransactionTagRepository`, assign/remove/observe use cases). New
  bottom-nav `SearchScreen`. Deleted Phase 3's `FinancialSearchEngine`.
  9 new tests, all passing. Full build gate green (`ktlintCheck detekt
  allTests assemble`) after fixing an SQLDelight FTS5 alias compile
  error, 3 ktlint violations, and 2 detekt `LongMethod` findings - see
  `05-current-state.md`/`14-search-engine.md` for detail. Memory bank
  updated: `02-system-architecture.md`, `03-domain-model.md`,
  `04-roadmap.md`, `05-current-state.md`, `07-repository-structure.md`,
  `14-search-engine.md`, `18-ui-design-system.md`,
  `26-active-initiatives.md`, this file. Committed as `25b7815`.

## Completed this session

- Implemented Phase 3 (Dashboard & UX) per ROADMAP.md: Dashboard, Net
  Worth, Cash Flow, Charts, Recent Activity, Search entry.
- Domain layer (`:domain`): `NetWorthCalculator` + `NetWorthSummary`,
  `CashFlowCalculator` + `CashFlowPeriod` (pure policy objects, same
  pattern as `TransactionLedgerRules`), `FinancialSearchEngine` +
  `SearchResult` (minimal in-memory substring search, Phase 4 will
  replace with FTS5). Four new use cases combining existing
  repositories via `Flow.combine`: `ObserveNetWorthUseCase`,
  `ObserveCashFlowUseCase`, `ObserveRecentTransactionsUseCase`,
  `SearchFinancialRecordsUseCase`. Added `kotlinx-datetime` 0.6.1 to
  `:domain` and `:composeApp` for correct calendar-month math.
- Zero `:data`/schema changes - Phase 3 reads Phase 2's persisted data
  only.
- Presentation (`:composeApp`): new `format/` package (`MoneyFormatter`,
  `MonthLabelFormatter`, `DateFormatter`, all locale-API-free) and new
  `dashboard/` package - `DashboardScreen` assembling Net Worth (+ Asset
  Allocation donut chart), Cash Flow (+ grouped bar chart), Recent
  Activity, Search, and Trust Indicators sections. Both charts are
  custom Canvas draws (no third-party charting library), fixed
  never-cycled categorical colors, every colored element paired with an
  icon/marker + text label (SystemPrompt Part 3, "never color alone").
  Retired Phase 0's `SystemStatusScreen`; `App.kt` now renders
  `DashboardScreen()`.
- Tests: 5 new domain unit-test files, all passing.
- Full build gate (`ktlintCheck detekt allTests assemble`, Android +
  Desktop) green: `BUILD SUCCESSFUL in 6m 21s`, 394 tasks. Every
  new/modified file is under 300 lines (largest: 102).
- Updated memory bank: `02-system-architecture.md`, `03-domain-model.md`,
  `04-roadmap.md`, `05-current-state.md`, `07-repository-structure.md`,
  `14-search-engine.md`, `18-ui-design-system.md`,
  `26-active-initiatives.md`, this file.

## Notable issues hit and fixed this session

- First full-gate run failed: detekt `LongParameterList` (3 findings,
  threshold 6) on `AssetAllocationDonutChart.drawSlice` (6 params),
  `CashFlowBarChart.drawBar` (7 params), and a `CashFlowCalculatorTest`
  helper (6 params: `year, month, day` instead of one `occurredAt`).
  Fixed by bundling geometry into a `Rect` (donut) and a private
  `BarGeometry` data class (bar chart), and collapsing the test
  helper's `year/month/day` into a single `occurredAt: Long` computed
  at each call site via the existing `epochMillis()` helper. Re-run was
  green.
- Self-caught during implementation (not build failures): a garbled
  import typo in `AppModule.kt`, fully-qualified type references instead
  of imports in two chart files, dead code in `RecentActivitySection.kt`,
  an experimental-API risk (`FlowRow`) in `TrustIndicatorsSection.kt`
  simplified to a plain `Row`, and a Compose anti-pattern (mutating
  state during composition) in `DashboardScreen.kt` fixed via
  `LaunchedEffect`.
- `dataviz` skill's `validate_palette.js` could not run (`node` not
  found in this environment) - proceeded without formal CVD validation,
  relying on the existing icon+text+color convention instead.

## Not completed

- Same Phase 1 items as before (Android instrumented test,
  `androidx.security.crypto` deprecation, Desktop key-file hardening) -
  all explicit Phase 8 scope, unchanged.
- Formal CVD palette validation (blocked on `node` availability, not
  blocking Phase 3 completion).
- Phase 4 (Search: FTS5, global search, filters, timeline search, tags)
  has not been started.

## Next recommended task

1. Owner review of Phase 3 (dashboard UX, chart design choices, the
   deliberately minimal search entry point) and explicit approval to
   start Phase 4.
2. Phase 4 (Search): replace `FinancialSearchEngine` with SQLite FTS5,
   add global search, filters, timeline search, and tags.
3. When an Android emulator/device is available, add the instrumented
   test flagged since Phase 1.
4. When `node`/npm becomes available, run `validate_palette.js` against
   the dashboard's chart colors.

## Open risks

- Same as Phase 1 (AGP 9 compatibility flags' shelf life,
  `androidx.security.crypto` deprecation) - unchanged.
- No ID-generation strategy has been chosen yet for new financial
  entities; record-creation UI (as opposed to the read-only Dashboard)
  will need one before users can create records through the app -
  still open, carried over from Phase 2.
