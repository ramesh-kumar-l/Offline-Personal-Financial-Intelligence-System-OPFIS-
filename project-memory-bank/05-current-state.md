# Current State

Last updated: 2026-07-17 (Phases 0-11 closed. Phase 11 - Testing - ran
the first real build gate against Phases 9/10's previously-unverified
code, found and fixed several real bugs, added new tests, and left
`./gradlew ktlintCheck detekt allTests assemble` green for both
Android and Desktop - see the Phase 11 section below and
`19-testing-strategy.md`)

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

### Phase 7 - Local AI (implemented and tested)

- Domain: `LocalAiPort` (`answer(question, asOfEpochMillis): AiAnswer`)
  is the local-model abstraction; its only binding,
  `RuleBasedLocalAiEngine` (`ai/engine/`), is a deterministic rule
  engine, not a neural model - no model weights can be downloaded in
  this offline sandboxed environment, a scope decision confirmed by the
  user before implementation began (see `15-ai-runtime.md`). The engine
  classifies question intent via `AiIntentClassifier` (keyword-based:
  `NET_WORTH`/`CASH_FLOW`/`SPENDING`/`BUDGET`/`GOAL`/`GENERAL`), builds
  a one-shot `FinancialSnapshot` (`BuildFinancialSnapshotUseCase`
  reading through a bundled `FinancialRepositories` holder - 7
  repositories in one constructor parameter, avoiding a detekt
  `LongParameterList` finding the same way Phase 5's
  `ImportDocumentRequest` did), and dispatches to one of six responder
  objects (`ai/engine/responder/`), each reusing an existing
  calculator/port rather than re-deriving logic: `NetWorthResponder`
  (Phase 3's `NetWorthCalculator`), `CashFlowResponder` (Phase 3's
  `CashFlowCalculator`), `SpendingResponder` (category-name keyword
  match over expense transactions), `BudgetResponder` (lists limits,
  explicitly states spend-to-date tracking is not implemented - a
  pre-existing Phase 2 gap), `GoalResponder` (current-vs-target
  progress), `GeneralResponder` (falls back to semantic retrieval).
  Every `AiAnswer(text, citations: List<AiCitation>)` cites the real
  `EntityType`/id/label it was derived from (ROADMAP "Explainable
  answers"). `RetrieveFinancialContextUseCase` (`ai/usecase/`) is the
  semantic-retrieval layer - lexical (Phase 4's FTS5 `SearchPort`), not
  vector-embedding-based, since no embedding model is available fully
  offline either; a deliberate, documented scope cut. `AiMoneyFormatter`
  intentionally duplicates `composeApp`'s `MoneyFormatter` since
  `:domain` cannot depend on the Presentation layer.
- No schema/`:data` changes - read-only phase, same pattern as Phase 3.
- Presentation: a 5th bottom-nav destination, "Assistant"
  (`composeApp/.../ai/AiAssistantScreen` + `AiAssistantScreenBody` +
  `AiCitationRow`) - a question box above a session-local (not
  persisted) conversation history.
- Tests: `AiIntentClassifierTest` (6 cases across all 6 intents),
  `RetrieveFinancialContextUseCaseTest` (mapping + limit, fake
  `SearchPort`), `RuleBasedLocalAiEngineTest` (5 cases: net worth
  citing accounts, spending narrowed to a mentioned category, budget
  listing limits without claiming spend status, goal progress, general
  fallback with no matches) - fakes for all 7 repositories + `SearchPort`
  following the established `FakeAccountRepository`-style pattern. All
  pass on both `desktopTest` and Android `testDebugUnitTest`. Every new
  file is under 300 lines (largest: `RuleBasedLocalAiEngineTest.kt`,
  ~190 lines).
- Full build gate (`ktlintCheck detekt allTests assemble`, Android +
  Desktop) green: `BUILD SUCCESSFUL in 1m 35s`, 395 tasks. Took 3
  attempts: (1) 20+ ktlint argument/parameter-wrapping and
  chain-method-continuation violations across 4 new files, fixed with
  `./gradlew ktlintFormat` (auto-fix) rather than hand-editing each one;
  (2) one detekt `MaxLineLength` in a KDoc comment on
  `CashFlowResponder.kt`, fixed by shortening the comment.

### Phase 8 - Security (implemented and tested)

- Domain: `domain/.../security/AutoLockPolicy.kt` - a pure idle-timeout
  policy object (`shouldLock(lastInteractionAt, now, timeoutMillis =
  5 min)`), same framework-free policy pattern as
  `TransactionLedgerRules`/`NetWorthCalculator`. `domain/.../audit/`
  follows the standard entity+port+usecase pattern: `AuditLogEntry`
  (`id, eventType, description, occurredAt`, append-only - no
  update/delete on the repository), `AuditEventType` (`APP_UNLOCKED`,
  `APP_UNLOCK_FAILED`, `BACKUP_EXPORTED`, `BACKUP_RESTORED`),
  `AuditLogRepository`, `RecordAuditEventUseCase`,
  `ObserveAuditLogUseCase`.
- Biometrics is presentation-layer, not a domain port - like Phase 5's
  `DocumentPicker`, `androidx.biometric.BiometricPrompt` needs a live
  `FragmentActivity`, which a Koin-injected domain port can't obtain.
  `composeApp/.../security/BiometricAuth.kt` declares
  `@Composable expect fun rememberBiometricAuthLauncher(onResult):
  () -> Unit` + a `BiometricAuthResult` sealed type (`Success`,
  `Failed`, `Error`, `NotAvailable`). Android's actual checks
  `BiometricManager.canAuthenticate` first and shows a real
  `BiometricPrompt` (biometric or device-credential fallback);
  `MainActivity` now extends `FragmentActivity` (was
  `ComponentActivity`) to host it. Desktop's actual always resolves to
  `NotAvailable` - there is no OS-uniform biometric API on the JVM, a
  documented gap, not a silent omission.
- Auto-lock: `composeApp/.../security/AppLockState.kt` is a `@Stable`
  presentation state holder - the app starts locked; navigating
  between bottom-nav destinations counts as "interaction" (a
  lighter-weight, deliberately simpler signal than raw touch-event
  tracking across every screen - see the gap note below) and re-arms
  the idle timer, checked once a second from `App.kt` via
  `AutoLockPolicy`. `LockScreen` (+ `LockScreenBody`) is the full-screen
  gate: tapping "Unlock" invokes the biometric launcher; `Success`
  unlocks and audits `APP_UNLOCKED`; `Failed`/`Error` audits
  `APP_UNLOCK_FAILED` and lets the user retry; `NotAvailable` (Desktop)
  switches to a "Confirm to unlock" manual fallback that still requires
  an explicit tap and is still audited (as a manual-confirmation
  unlock, distinctly worded from a biometric one), rather than silently
  bypassing the lock screen.
- Backup encryption: interpreted as hardening the key that both the
  live database *and* any future backup inherit their encryption from
  (ADR 0005's explicit Phase 8 follow-up), not as building Phase 9's
  portable passphrase-based backup UX. `DatabaseKeyProvider.desktop.kt`
  now restricts the generated key file to the owning OS account after
  writing it - POSIX permissions (`rw-------`) where supported, or a
  best-effort Windows ACL (single owner-only `AclEntry`) + hidden
  attribute otherwise - narrowing ADR 0005's flagged weak point ("any
  process on the machine could read the key file") to the owning user
  account. Full OS keychain/DPAPI integration remains unimplemented -
  see the gap note below. Android's key storage was already
  Keystore-backed (`EncryptedSharedPreferences`) since Phase 1;
  unchanged this phase. `ExportBackupUseCase`/`RestoreBackupUseCase`
  were deliberately **not** added - there is still no backup UI to call
  them from (Phase 9 owns that), and CLAUDE.md's "no speculative,
  unwired code" rule argues against adding use cases with no caller;
  `AuditEventType` already has `BACKUP_EXPORTED`/`BACKUP_RESTORED`
  ready for Phase 9 to record directly.
- Presentation: a 6th bottom-nav destination, "Security"
  (`composeApp/.../security/SecurityScreen` + `SecurityScreenBody` +
  `AuditLogRow`) - a policy-summary card above the audit trail list.
  `App.kt` now wraps the whole `Scaffold`/nav tree in a lock gate: a
  `LaunchedEffect` ticks every second calling
  `AppLockState.checkIdleTimeout()`; while `isLocked`, `LockScreen()`
  renders instead of the app content.
- New dependency: `androidx.biometric:biometric:1.1.0` (androidMain
  only; the latest stable release - `1.4.0` does not exist, only
  alpha07, confirmed against Google Maven's metadata before pinning).
- Tests: `AutoLockPolicyTest` (3 cases: before/at/well-past the
  timeout), `AuditLogUseCasesTest` (record delegates to the repository,
  observe returns the repository's stream) - fakes follow the
  established `FakeAccountRepository`-style pattern. All pass on both
  `desktopTest` and Android `testDebugUnitTest`. Every new file is
  under 300 lines (largest: `LockScreen.kt`, ~90 lines).
- Full build gate (`ktlintCheck detekt allTests assemble`, Android +
  Desktop) green: `BUILD SUCCESSFUL in 6m 2s`, 395 tasks. Took 3
  attempts: (1) `androidx.biometric:biometric:1.4.0` does not exist on
  Google Maven (only alpha pre-releases past `1.1.0`) - pinned to the
  actual latest stable, `1.1.0`; (2) one detekt `SpreadOperator` finding
  on `DatabaseKeyProvider.desktop.kt`'s ACL-permissions call - fixed by
  passing `AclEntryPermission.entries.toSet()` to the `Set`-typed
  overload instead of spreading a `toTypedArray()` into a vararg call;
  (3) `BUILD SUCCESSFUL in 6m 2s` - confirmed via the literal log text
  (the task-notification again claimed "exit code 0" on the failing
  first run - the same unreliable-signal pattern documented in every
  prior phase).

### Phase 9 - Import/Export (implemented, NOT build-verified)

- Domain: `ExportBackupUseCase`/`RestoreBackupUseCase`
  (`domain/.../backup/usecase/`) wrap Phase 1's `BackupPort` with the
  Application-layer boundary Phase 8 deliberately left for this phase.
  New `domain/.../importexport/` package: `FinancialDataSnapshot` (full
  entity-list bundle + `schemaVersion`/`exportedAtEpochMillis`),
  `TransactionTagAssignment` (stands in for the join table, no domain
  entity of its own), `ImportExportCoreRepositories`/
  `ImportExportRelatedRepositories` (7+6-field bundles keeping the
  export/import use cases under detekt's `LongParameterList`
  threshold), `TransactionCsvCodec` (hand-rolled RFC4180-ish
  quote-escaping, no new CSV dependency),
  `ExportFinancialDataUseCase`/`ImportFinancialDataUseCase` (JSON,
  every entity), `ExportTransactionsCsvUseCase`/
  `ImportTransactionsCsvUseCase` (CSV, transactions only - the one
  entity with a natural tabular shape, confirmed with the owner).
  Import writes independent entities first, then transactions via
  `FinancialLedgerPort` (never a raw upsert), then relational/dependent
  entities; `id`-preserving upsert/`INSERT OR REPLACE` semantics make
  re-importing idempotent. Every domain entity/enum in the snapshot
  gained a bare `@Serializable` (kotlinx.serialization, first use in
  this project); `AuditLogEntry` was deliberately excluded from the
  snapshot (audit history isn't "financial data"). `AuditEventType`
  gained `DATA_EXPORTED`/`DATA_IMPORTED`. `RelationshipRepository`
  gained `observeAll()` (previously only `observeInvolving`) - the one
  port that had no existing "read everything" primitive.
- Data: `Relationship.sq` gained a `selectAll` query;
  `SqlRelationshipRepository.observeAll()` implements it. No schema
  migration - Phase 9 persists no new tables/columns.
  `FileBackupPort.restoreBackup` (both `androidMain`/`desktopMain`
  actuals) now calls `driver.close()` itself before copying the backup
  file over the live database file - required on Windows (an open
  handle blocks overwriting) and everywhere else (a live connection
  must never keep pointing at a swapped-out file). This means a
  successful restore leaves every Koin-held `OpfisDatabase`/repository
  singleton bound to a closed driver - there is no in-process DI-graph
  reload, so the UI must treat restore as requiring a full app restart.
- Presentation: a 7th bottom-nav destination, "Data"
  (`composeApp/.../importexport/ImportExportScreen` +
  `ImportExportScreenBody`) with six actions (export/import x JSON,
  CSV, encrypted backup) and a confirmation dialog before restore
  (destructive + closes the app). Three new `composeApp/.../io/`
  platform abstractions, same `expect`/`actual` shape as
  `DocumentPicker`: `FileSaver` (native "save file" dialog - generic
  byte writer, one instance per mime type since Android's
  `CreateDocument` fixes its mime type at composition time), `TempFile`
  (staging file path + byte read/write - both platforms always stage
  backup export/restore through a temp file, even though Desktop's
  `FileDialog` could technically hand back a real path directly, so
  every export flow shares one identical code path instead of
  branching per platform), `AppExit` (process termination after a
  successful restore - Desktop `exitProcess(0)`, Android
  `FragmentActivity.finishAffinity()` + `Process.killProcess`, same
  activity-casting precedent as Phase 8's `BiometricAuth.android.kt`).
  Imports reuse the existing `rememberDocumentPickerLauncher` (already
  reads any picked file to bytes) for all three import flows - no new
  "open" abstraction was needed.
- Tests: `TransactionCsvCodecTest` (round-trip incl. comma/quote/
  newline escaping, null optional fields, empty/header-only CSV),
  `BackupUseCasesTest` (audit-free thin-wrapper delegation + failure
  passthrough), `ImportExportUseCasesTest` (one full export-then-import
  round-trip through every entity type, using fakes for all 12
  repositories + the ledger port - fakes live in a sibling
  `ImportExportFakes.kt` to keep the test file itself under 300 lines),
  `TransactionCsvUseCasesTest` (CSV export/import round-trip). Extended
  `SqlRelationshipRepositoryTest` with an `observeAll` case and
  `EncryptedPersistenceRecoveryTest` with a case proving `restoreBackup`
  closes the driver (further queries against it throw) - the existing
  round-trip test's manual `driver.close()` before restore was removed
  since `restoreBackup` now does that itself.
- **This phase's build gate could not be run.** The session's
  environment has no JDK, no Android SDK, no prior `.gradle` cache, and
  no `java`/`javac` on `PATH` (confirmed by direct filesystem/registry
  search) - a materially different toolchain than every prior phase
  documented in this file. All code was manually reviewed line-by-line
  instead (types, imports, detekt `LongParameterList`/constructor
  thresholds, `expect`/`actual` signature matching, kotlinx.serialization
  import correctness - one real bug, a missing
  `kotlinx.serialization.decodeFromString` import, was caught and fixed
  this way). `kotlinx-serialization-json`'s pinned version (1.8.0) was
  not confirmed against real Maven Central metadata, unlike Phase 8's
  `androidx.biometric` precedent - this is the single highest-risk
  unknown for the first real build. **Running
  `./gradlew ktlintCheck detekt allTests assemble` on a machine with
  the real toolchain is the required next step before treating Phase 9
  as done** - see `06-tech-stack.md`.

### Phase 10 - Performance (implemented, NOT build-verified)

- Four targeted changes, each backed by a real existing hot path (see
  `20-performance-budget.md` for full rationale and the two items
  deliberately deferred - WAL pragmas and additional indexes that no
  query actually uses):
  1. `TransactionRepository` gained `observeRecent(limit)` - a bounded,
     indexed `SELECT ... ORDER BY occurred_at DESC LIMIT :limit`
     replacing `ObserveRecentTransactionsUseCase`'s previous
     `observeAll()` + in-memory `sortedByDescending().take()` (an
     unbounded full-table load re-run on every emission, on the
     Dashboard's busiest widget).
  2. Schema v8 (`migrations/7.sqm`): two new indexes on
     `financial_transaction` - `occurred_at` (serves `selectRecent`/
     `selectAll`/`selectByAccount`'s existing `ORDER BY`) and
     `transfer_account_id` (serves `selectByAccount`'s existing `OR`
     clause, previously unindexed on that side). No new tables.
  3. `Main.kt` (Desktop) and `OpfisApplication.kt` (Android) now launch
     a background `Dispatchers.IO` coroutine right after `startKoin`
     to pre-warm the `OpfisDatabase` singleton (encrypted driver open +
     schema create/migrate), so the first screen's `koinInject()` call
     doesn't block the Compose UI/composition thread on cold DB open -
     it overlaps with the time the user spends on the lock screen.
  4. `App.kt`'s auto-lock idle-check poll interval dropped from 1s to
     15s (`AUTO_LOCK_CHECK_INTERVAL_MILLIS`) - still far below the
     5-minute `AutoLockPolicy` timeout, ~15x fewer wakeups.
- All 5 implementers of `TransactionRepository` (the real
  `SqlTransactionRepository` + 4 test fakes across
  `ObserveRecentTransactionsUseCaseTest`, `ObserveTimelineUseCaseTest`,
  `RuleBasedLocalAiEngineTest`, `ImportExportFakes`) updated for the new
  interface method. New `SqlTransactionRepositoryTest` (observeRecent
  ordering/limit) and an extended `SchemaMigrationTest` v1-to-current
  assertion proving `migrations/7.sqm` applies cleanly.
- Incidental doc fix while touching schema-version history:
  `11-database-schema.md`'s "Schema versions" list was missing the
  Phase 8 `audit_log` migration (v7) entirely - filled in alongside
  this phase's own v8 entry.
- **This phase's build gate could not be run**, same constraint as
  Phase 9 (no JDK/Android SDK in this session's environment) - manually
  reviewed instead. One real mistake was caught and fixed this way: a
  test call to the generated `selectRecent(10)` query passed a bare
  `Int` literal where SQLDelight generates a `Long` parameter for
  untyped `LIMIT` bind values - Kotlin does not auto-widen `Int` to
  `Long`, so this would have been a compile error (`10` -> `10L`).
  No profiler or benchmark tool is available in this environment
  either, so `20-performance-budget.md`'s three named targets (cold
  start, search, dashboard render) are structurally targeted, not
  empirically confirmed.

### Phase 11 - Testing (implemented and build-verified)

- **JDK 21 + Android SDK 36 became available this session** (installed
  by the owner). First action: ran the long-overdue
  `./gradlew ktlintCheck detekt allTests assemble` against Phases 9 and
  10's previously-unverified code. Found and fixed real issues across
  several iterations:
  1. 15 detekt violations in Phase 9/10 files (mostly `MaxLineLength`/
     `LongMethod` from lines that read fine by eye but exceeded the
     configured limits once actually linted; one `ForEachOnRange`).
     `ImportExportScreen.kt` was split into `ImportExportScreen.kt` +
     new `ImportExportLaunchers.kt` to bring its main composable under
     the `LongMethod` threshold (60 lines) - extracted
     `ImportExportContext`/`ExportUseCases`/`ImportExportUseCases`/
     `ExportSavers`/`ImportPickers` bundles, matching the existing
     `ImportExportActions` bundling pattern.
  2. **Real compile bug**: `FakeRelationshipRepository` in
     `ObserveKnowledgeGraphUseCaseTest.kt` was missing the
     `observeAll()` override that Phase 9 added to
     `RelationshipRepository` - only the `ImportExportFakes.kt` fake had
     been updated, this one was missed.
  3. **Real test bug**: `EncryptedPersistenceRecoveryTest`'s
     `restoreBackup closes the driver so it cannot be used afterward`
     asserted that querying a closed `SqlDriver` throws - it doesn't,
     on Desktop's `io.github.willena:sqlite-jdbc` driver, which
     transparently reopens a connection on next use. Rewritten to
     assert the invariant that actually matters (the reopened
     connection reads the restored file, not stale data); doc comments
     on `FileBackupPort` (both platforms) and `17-backup-engine.md`
     corrected to match - see `19-testing-strategy.md`.
  4. **Real type-inference bug** in a new test:
     `assertEquals(listOf("doc-1" to "tx-2"), repository.linked)` failed
     to compile because `listOf(...)` inferred
     `List<Pair<String, String>>` while `repository.linked` was
     `List<Pair<String, String?>>` - invariant generics couldn't unify.
     Fixed with an explicit type argument.
- **New tests closing coverage gaps** (see `19-testing-strategy.md` for
  full detail): consolidated `XUseCasesTest` files for every
  previously-untested `:domain` CRUD feature (account, asset, budget,
  category, liability, goal, tag, memory, relationship, document,
  cashflow, ai) - roughly 40 use cases that had zero tests before this
  phase; `SqlAuditLogRepositoryTest` (the one `Sql*Repository` with no
  test - security-relevant); `LockScreenBodyTest`, `:composeApp`'s
  first-ever test of any kind, using the new
  `org.jetbrains.compose.ui:ui-test` dependency and headless
  `runComposeUiTest` (no Robolectric/Android instrumentation needed).
- **Naming collision gotcha discovered**: `private class FakeXRepository`
  is file-scoped, but two files in the *same package* declaring the
  identical name still collide at compile time. Hit this twice
  (`FakeDocumentRepository`, `FakeRelationshipRepository` both already
  existed under different test files in their packages) - fixed by
  renaming the newly-added ones.
- Full build gate (`ktlintCheck detekt allTests assemble`, Android +
  Desktop) green: `BUILD SUCCESSFUL in 59s`, 400 tasks - now covers
  Phases 0-11, not just 0-8 as of the last confirmed-green run.
- **Not done this phase** (deliberate scope cuts, not oversights - see
  `19-testing-strategy.md`): no automated performance benchmark harness
  (targets remain structurally justified, not measured); UI test
  coverage is one screen (`LockScreenBody`) out of ~10, since the
  others Koin-inject use cases directly and would need either a test
  Koin module or further screen/body splitting; no Android instrumented
  tests (still no emulator/device in this environment).

## Known gaps / not yet verified

- Android's encrypted driver path has no instrumented test (no
  emulator/device in this environment) - unchanged from Phase 1.
- `androidx.security.crypto` deprecation remains open; Desktop key-file
  hardening got a partial Phase 8 pass (owner-only file permissions/ACL)
  but full OS keychain/DPAPI integration is still not implemented - see
  the Phase 8 section above and `09-security-model.md`.
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
- Phase 7's `LocalAiPort` has no real local LLM/embedding-model
  binding - `RuleBasedLocalAiEngine` is deterministic, not neural.
  `AiIntentClassifier` is a fixed English keyword list with no fuzzy
  matching or confidence scoring. The AI conversation history is
  session-only, not persisted, and does not create `MemoryEvent`s.
- Phase 8's Desktop biometric fallback is a manual "Confirm to unlock"
  tap, not real authentication - there is no OS-uniform biometric API
  on the JVM. Auto-lock's "interaction" signal is bottom-nav navigation
  only, not raw touch/scroll tracking across every screen (a
  deliberate simplification to avoid a global pointer-event interceptor
  that could interfere with existing gesture handling - see
  `09-security-model.md`). Desktop key-file hardening is owner-only
  file permissions/ACL, not full OS keychain/DPAPI integration.
  `ExportBackupUseCase`/`RestoreBackupUseCase` were not built this
  phase (no UI to call them from yet - `AuditEventType` is ready for
  Phase 9 to use). The audit log has no UI to prune/export it and no
  retention policy (append-only, grows unbounded).
- Phase 9's build gate is now green (Phase 11, see above), but its
  functional design choices are unchanged: CSV import/export is
  transactions-only by design (see `03-domain-model.md`); JSON import
  recreates `Document` rows as metadata/extracted-text only, never the
  underlying file bytes; restoring an encrypted backup requires a full
  app restart (no in-process DI-graph reload); the audit log's
  `DATA_EXPORTED`/`DATA_IMPORTED` entries record the action but not a
  diff/summary (`ImportSummary`'s counts are UI-only, not persisted
  alongside the audit entry).
- Phase 10's build gate is now green too. SQLite `WAL`/
  `synchronous=NORMAL` pragmas remain deliberately not enabled -
  untested interaction risk with Phase 9's restore flow (stale
  `-wal`/`-shm` sidecar files aren't cleaned up by `File.copyTo`), see
  `20-performance-budget.md`. No profiler/benchmark harness was added
  in Phase 11 either (see below), so the three named performance
  budgets (cold start, search, dashboard render) remain structurally
  targeted but unmeasured.
- Phase 11's own gaps: UI test coverage is one screen out of ~10; no
  automated performance benchmarks; no Android instrumented tests - see
  the Phase 11 section above and `19-testing-strategy.md`.

## Pending

- **Immediate**: manually time cold start, search latency, and
  dashboard render on a real device/desktop against
  `20-performance-budget.md`'s targets - no automated benchmark harness
  exists, and this is the one part of Phase 10's exit criterion
  ("performance budgets achieved") still unconfirmed now that the build
  itself is verified.
- Expand UI test coverage beyond `LockScreenBody` to the other ~9
  screens - most already follow the `XScreen`/`XScreenBody` split, so
  the `XScreenBody` composables (pure layout, no Koin) are the natural
  next candidates - see `19-testing-strategy.md`.
- Phase 12 onward (see `04-roadmap.md` / `ROADMAP.md`): Phase 12 "MVP
  Release" (documentation, demo, release notes, packaging, v1.0) - not
  yet started, awaiting owner review per ROADMAP.md's "stop for review
  before the next phase" policy.
