# Session Handoff

Last session: 2026-07-16

## Completed in Phase 10 session (Performance)

- Implemented Phase 10 per ROADMAP.md (query/startup/battery
  optimization), same session as Phase 9, continuing past that phase's
  review checkpoint per the owner's explicit "continue with next Phase"
  instruction. Four targeted changes, each backed by a real, existing
  hot path found by reading the actual schema/DI/composition-root code
  rather than invented speculatively - full rationale in
  `20-performance-budget.md`:
  1. `TransactionRepository.observeRecent(limit)` (new interface
     method) replaces `ObserveRecentTransactionsUseCase`'s previous
     `observeAll()` + in-memory `sortedByDescending().take()` - the
     Dashboard's "Recent Activity" widget no longer loads the entire
     transaction table into a Kotlin `List` on every emission just to
     keep the newest 10. Backed by a new indexed SQL query
     (`selectRecent`, schema v8 via `migrations/7.sqm`).
  2. Schema v8 also adds a `transfer_account_id` index -
     `selectByAccount`'s existing `WHERE account_id = ? OR
     transfer_account_id = ?` only had an index on the first side.
  3. `Main.kt`/`OpfisApplication.kt` pre-warm the `OpfisDatabase` Koin
     singleton on a background `Dispatchers.IO` coroutine right after
     `startKoin`, so cold DB open (SQLCipher key derivation + schema
     migration) doesn't block the first screen's composition on the
     main/UI thread - it now overlaps with the lock-screen unlock
     interaction instead.
  4. `App.kt`'s auto-lock idle-check poll interval: 1s -> 15s (still
     far below the 5-minute timeout).
- Deliberately did **not** enable SQLite `WAL`/`synchronous=NORMAL`
  pragmas, despite being a well-known SQLite performance win - Phase
  9's `restoreBackup` copies the main `.db` file over the live one but
  doesn't checkpoint/delete `-wal`/`-shm` sidecar files, so enabling WAL
  now would introduce an untested restore-corruption risk this session
  cannot verify. Also skipped adding indexes to `budget.category_id`/
  `category.parent_id` - no existing query filters on either column, so
  an index there would be speculative dead weight, not a real
  optimization (CLAUDE.md "no speculative code").
- Updated all 5 places implementing `TransactionRepository` for the new
  interface method: the real `SqlTransactionRepository` plus 4 test
  fakes (`ObserveRecentTransactionsUseCaseTest`,
  `ObserveTimelineUseCaseTest`, `RuleBasedLocalAiEngineTest`,
  `ImportExportFakes`). New `SqlTransactionRepositoryTest`
  (`observeRecent` ordering/limit) and an extended `SchemaMigrationTest`
  assertion proving `migrations/7.sqm` applies cleanly on top of the
  existing v1-to-current chain.
- **Same missing-JDK/Android-SDK constraint as Phase 9** - the build
  gate could not be run, so every file was manually read back and
  checked by hand instead. One real bug was caught and fixed this way:
  a test called the generated `selectRecent(10)` query with a bare
  `Int` literal, but SQLDelight generates a `Long` parameter for
  untyped `LIMIT` bind values and Kotlin does not auto-widen `Int` to
  `Long` - would have been a compile error (fixed to `10L`).
  `SqlTransactionRepository.observeRecent`'s own `limit.toLong()` call
  was already correct, so this was caught only in the test file, not
  production code.
- Incidental fix while touching schema-version history in
  `11-database-schema.md`: filled in a pre-existing gap where the
  Phase 8 `audit_log` migration (v7) was missing from the "Schema
  versions" list entirely (only relevant since this phase was already
  adding its own v8 entry to the same list).
- Updated memory bank: `02-system-architecture.md`,
  `04-roadmap.md`, `05-current-state.md`, `11-database-schema.md`,
  `20-performance-budget.md` (expanded from a 2-line stub),
  `26-active-initiatives.md`, this file.

## Not completed (as of end of Phase 10 session)

- **The build gate has never been run against Phase 9 or Phase 10** -
  same unverified status as before, now covering two phases' worth of
  changes.
- **No profiler or benchmark tool exists in this environment** -
  `20-performance-budget.md`'s three named targets (cold start <1s,
  search <100ms, dashboard <300ms) were never measured, only targeted
  structurally. This is a materially bigger gap for Phase 10 than the
  missing build gate was for Phase 9, since "performance budgets
  achieved" is this phase's literal ROADMAP exit criterion and cannot
  be claimed met without a real measurement.
- SQLite WAL/synchronous pragmas were considered and deferred, not
  implemented - see above and `20-performance-budget.md`.
- No pagination was added for the audit log (unbounded `selectAll`,
  known gap since Phase 8) or the JSON export/import snapshot (loads
  every entity into one in-memory string, known gap since Phase 9) -
  both out of scope for this pass.

## Next recommended task (as of end of Phase 10 session)

1. **Run `./gradlew ktlintCheck detekt allTests assemble` on a machine
   with a real JDK + Android SDK** and fix whatever it surfaces - now
   covers two unverified phases (9 and 10), not optional cleanup.
2. **Measure actual performance** once the build is green: cold start,
   search latency, dashboard render time, compared against
   `20-performance-budget.md`'s targets. Without this, Phase 10's exit
   criterion ("performance budgets achieved") is unconfirmed.
3. Owner review of both phases and explicit direction on Phase 11
   (Testing) - the standing ROADMAP.md policy is to stop for review
   between phases; this session's owner instruction authorized
   continuing through Phase 10 only.
4. When an Android emulator/device is available, add the instrumented
   test flagged since Phase 1 and verify Phase 5's OCR/picker paths.
5. When `node`/npm becomes available, run `validate_palette.js` against
   the dashboard's chart colors.

## Completed in Phase 9 session (Import/Export)

- Implemented Phase 9 per ROADMAP.md: CSV (transactions only), JSON
  (every entity), and encrypted-backup export/restore, continuing past
  the Phase 8 review checkpoint per the owner's explicit instruction in
  the message that opened this session ("continue the implementation").
  New `domain/.../importexport/` package (`FinancialDataSnapshot`,
  `TransactionTagAssignment`, `ImportExportCoreRepositories`/
  `ImportExportRelatedRepositories`, `TransactionCsvCodec`,
  `importexport/usecase/` [`ExportFinancialDataUseCase`,
  `ImportFinancialDataUseCase` + `ImportSummary`,
  `ExportTransactionsCsvUseCase`, `ImportTransactionsCsvUseCase`]) and
  `domain/.../backup/usecase/` (`ExportBackupUseCase`,
  `RestoreBackupUseCase` wrapping Phase 1's `BackupPort`). Every domain
  entity/enum touched by the JSON snapshot gained a bare `@Serializable`
  (kotlinx.serialization, first use in this project) rather than a
  parallel DTO hierarchy. `RelationshipRepository` gained `observeAll()`
  (previously only `observeInvolving`) - the one port with no existing
  "read everything" primitive, needed for full-dataset export.
  `FileBackupPort.restoreBackup` (both platform actuals) now closes its
  own driver before copying, since a successful restore requires the
  whole app process to exit and restart (no in-process Koin-graph
  reload) - `composeApp/.../io/AppExit` (new) does the actual
  termination, `composeApp/.../io/FileSaver` and `.../io/TempFile` (new)
  back the save-dialog and Android-SAF-Uri-isn't-a-path staging steps.
  New 7th bottom-nav destination, "Data" (`ImportExportScreen` +
  `ImportExportScreenBody`). Full detail in `05-current-state.md` and
  `17-backup-engine.md` (fleshed out from a 2-line stub).
- Scope decisions made explicit, two confirmed via `AskUserQuestion`
  before coding began: (1) this pass covers Phase 9 only, then stops
  for review before Phase 10 (Performance) - not a push through Phases
  9-12 in one continuous session; (2) CSV is scoped to transactions
  only (the one entity with a natural tabular shape), JSON covers every
  entity for full portability - both per explicit owner confirmation,
  see `17-backup-engine.md`. Also decided without a question (directly
  justified by the task at hand, matching CLAUDE.md's "no speculative
  code" guidance): audit-entry id/timestamp generation for the new
  `DATA_EXPORTED`/`DATA_IMPORTED` events lives in `ImportExportScreen.kt`
  (presentation layer), mirroring Phase 8's `LockScreen.kt` precedent
  exactly, rather than inside the new domain use cases - domain use
  cases elsewhere in this codebase always receive fully-formed entities
  from the call site (e.g. `UpsertAccountUseCase` never computes
  `createdAt` itself), so this keeps the new use cases consistent with
  that convention instead of introducing a new pattern.
- **This session's environment has no JDK, no Android SDK, and no
  prior Gradle cache** - confirmed by direct search (no `java`/`javac`
  on `PATH`, no JDK under `Program Files`/`.jdks`/the registry, no
  `~/.gradle` directory), a materially different toolchain than every
  prior phase documented in this file (which ran against "JDK 25.0.3,
  Android SDK 36..."). `./gradlew ktlintCheck detekt allTests assemble`
  could not be run. Surfaced this to the owner via `AskUserQuestion`
  before proceeding; owner chose "manual review only, build later."
  Every new/changed file was then read back in full and checked by hand
  for: import correctness, `expect`/`actual` signature matching, detekt
  `LongParameterList`/constructor-threshold risk (kept every new
  bundling data class/composable at <=7 fields after the DocumentVault/
  SearchScreen precedent from Phase 5 showed the threshold fires *at*
  the limit, not only above it), and Kotlin syntax. One real bug was
  caught this way: `ImportFinancialDataUseCase.kt` called
  `Json.decodeFromString<FinancialDataSnapshot>(json)` without
  importing the reified extension function
  `kotlinx.serialization.decodeFromString` (present in the sibling
  `ExportFinancialDataUseCase.kt`'s `encodeToString` import but missed
  on the decode side) - fixed by adding the import. **This is not a
  substitute for a real compile** - the next session (or the owner)
  must run the actual build gate before trusting this phase as green;
  the highest-risk unknown is whether the freshly-added
  `kotlinx-serialization-json` version (1.8.0, chosen from general
  knowledge, not confirmed against real Maven Central metadata unlike
  Phase 8's `androidx.biometric` precedent) actually resolves.
- Every new file is under 300 lines (largest: `ImportExportFakes.kt`
  at ~230 lines - split out of what was originally one 318-line test
  file once the strict 300-line modularity rule flagged it, moving all
  13 fake repository classes to their own file so the test file itself
  stayed focused and short).
- Updated memory bank: `02-system-architecture.md`, `03-domain-model.md`,
  `04-roadmap.md`, `05-current-state.md`, `06-tech-stack.md`,
  `07-repository-structure.md`, `17-backup-engine.md` (fleshed out from
  a 2-line stub), `26-active-initiatives.md`, this file.

## Not completed (as of end of Phase 9 session)

- **The build gate has never been run against this phase's code** - see
  above. Treat every claim of correctness in this session as
  "manually reviewed," not "compiler/test verified," until that
  changes.
- No progress indicator for large exports/imports; no dry-run/preview
  before a JSON/CSV import applies (only encrypted-backup restore has a
  confirmation dialog, since it alone is destructive).
- CSV import/export remains transactions-only by design (see
  `17-backup-engine.md`) - no other entity gained CSV support.
- Restoring an encrypted backup still requires a full app-process
  restart - no in-process Koin/DI-graph reload was attempted (judged
  too large/risky for one session).
- Same Phase 1/3/5/6/7/8 items as before (Android instrumented test,
  `androidx.security.crypto` deprecation, CVD palette validation,
  OCR/`DocumentPicker` device verification, `Relationship`/
  `KnowledgeGraph` presentation layer, no real local LLM binding, full
  OS keychain/DPAPI integration for Desktop's key) - all unchanged, see
  the Phase 8 session block below for detail.

## Next recommended task (as of end of Phase 9 session)

1. **Run `./gradlew ktlintCheck detekt allTests assemble` on a machine
   with a real JDK + Android SDK** and fix whatever it surfaces - this
   is the mandatory first step, not optional cleanup, since Phase 9 has
   never been compiled.
2. Owner review of Phase 9 (import/export scope, the CSV-transactions-
   only cut, the mandatory-app-restart restore UX) and explicit
   direction on Phase 10 (Performance) - the standing ROADMAP.md policy
   is to stop for review between phases; this session's owner
   instruction authorized continuing only through Phase 9.
3. When an Android emulator/device is available, add the instrumented
   test flagged since Phase 1 and verify Phase 5's OCR/picker paths.
4. When `node`/npm becomes available, run `validate_palette.js` against
   the dashboard's chart colors.

## Completed in Phase 8 session (Security)

- Implemented Phase 8 per ROADMAP.md: biometrics, auto-lock, backup
  encryption (interpreted as key-storage hardening, see below), audit
  log. New `domain/.../security/AutoLockPolicy.kt` (pure idle-timeout
  policy) and `domain/.../audit/` (`AuditLogEntry`/`AuditEventType`,
  `AuditLogRepository`, `RecordAuditEventUseCase`,
  `ObserveAuditLogUseCase`) following the standard entity+port+usecase
  pattern. Biometrics lives in `composeApp/.../security/` (not
  `:domain`) since `androidx.biometric.BiometricPrompt` needs a live
  `FragmentActivity` - same reasoning as Phase 5's `DocumentPicker`:
  `BiometricAuth.kt` (`@Composable expect fun
  rememberBiometricAuthLauncher`), Android actual (real
  `BiometricPrompt`, `MainActivity` changed from `ComponentActivity` to
  `FragmentActivity`), Desktop actual (always `NotAvailable` - no
  OS-uniform biometric API on the JVM). `AppLockState` +
  `LockScreen`/`LockScreenBody` gate the whole app (`App.kt` wraps the
  bottom-nav `Scaffold`; starts locked; a 1s `LaunchedEffect` ticks
  `AutoLockPolicy` against the last bottom-nav interaction). New 6th
  bottom-nav destination, "Security" (`SecurityScreen` +
  `SecurityScreenBody` + `AuditLogRow`), showing a policy summary and
  the audit trail. Full detail in `05-current-state.md` and
  `09-security-model.md` (fleshed out from a 2-line stub).
- Scope decisions made explicit (no `AskUserQuestion` needed this
  session - all resolvable from ADR 0005's own stated Phase 8
  follow-ups and CLAUDE.md's "no speculative/unwired code" rule):
  (1) "Backup encryption" was interpreted as hardening the key
  `BackupPort`'s exports already inherit their encryption from (ADR
  0005 explicitly flagged Desktop's plain-file key as a Phase 8
  follow-up), not as building Phase 9's portable passphrase-based
  backup UX - `DatabaseKeyProvider.desktop.kt` now restricts the key
  file to the owning OS account (POSIX `rw-------` or a best-effort
  Windows ACL/hidden-attribute fallback); (2) `ExportBackupUseCase`/
  `RestoreBackupUseCase` were deliberately not built - there is still
  no backup UI to call them from (that's Phase 9's job), and adding
  unwired use cases would violate CLAUDE.md's "no speculative code"
  guidance - `AuditEventType` already has `BACKUP_EXPORTED`/
  `BACKUP_RESTORED` ready for Phase 9 to record directly; (3) auto-lock
  treats bottom-nav navigation as "interaction," not raw touch/scroll
  events - a deliberately simpler signal than a global pointer-event
  interceptor, which risked interfering with existing scroll/gesture
  handling across all 6 screens for uncertain benefit; (4) Desktop's
  biometric fallback is an explicit "Confirm to unlock" manual tap
  (still audited, distinctly worded from a biometric unlock), not a
  silent bypass of the lock screen.
- The build gate took 3 attempts to go green: (1) pinned
  `androidx.biometric:biometric:1.4.0`, which does not exist on Google
  Maven (only alpha pre-releases go past `1.1.0` - confirmed by
  fetching Google Maven's `maven-metadata.xml` directly rather than
  guessing) - repinned to the real latest stable, `1.1.0`; (2) one
  detekt `SpreadOperator` finding on
  `DatabaseKeyProvider.desktop.kt`'s ACL-permissions call
  (`*AclEntryPermission.entries.toTypedArray()`) - fixed by calling the
  `Set`-typed overload directly (`AclEntryPermission.entries.toSet()`)
  instead of spreading an array into a vararg call; (3)
  `BUILD SUCCESSFUL in 6m 2s`, 395 tasks - confirmed via the literal
  log text, and cross-checked that both new test files
  (`AutoLockPolicyTest`: 3 cases, `AuditLogUseCasesTest`: 2 cases) show
  `failures="0" errors="0"` in both `desktopTest` and Android
  `testDebugUnitTest` result XML. The task-notification again falsely
  reported "exit code 0" on the failing first run - same unreliable
  signal documented in every prior phase; never trust it over the
  log's own `BUILD SUCCESSFUL`/`BUILD FAILED` text.
- Every new file is under 300 lines (largest: `LockScreen.kt`, ~90
  lines).
- Updated memory bank: `02-system-architecture.md`, `03-domain-model.md`,
  `04-roadmap.md`, `05-current-state.md`, `06-tech-stack.md`,
  `07-repository-structure.md`, `09-security-model.md` (fleshed out
  from a 2-line stub), `18-ui-design-system.md`,
  `26-active-initiatives.md`, this file.

## Not completed (as of end of Phase 8 session)

- No real OS keychain/DPAPI integration for Desktop's database key -
  only owner-only file permissions/ACL hardening, which narrows but
  does not close ADR 0005's flagged weak point.
- Desktop has no real biometric authentication - the JVM has no
  OS-uniform biometric API, so its lock screen falls back to a manual
  "Confirm to unlock" tap.
- Auto-lock's interaction signal is bottom-nav navigation only, not
  comprehensive touch/scroll tracking across every screen.
- `ExportBackupUseCase`/`RestoreBackupUseCase` do not exist yet -
  intentionally deferred to Phase 9, which owns the actual backup/
  restore UI; `AuditEventType.BACKUP_EXPORTED`/`BACKUP_RESTORED` are
  ready for it to use.
- The audit log has no retention policy or UI to prune/export it -
  append-only, grows unbounded.
- Same Phase 1/3/5/6/7 items as before (Android instrumented test,
  `androidx.security.crypto` deprecation, CVD palette validation,
  OCR/`DocumentPicker` device verification, `Relationship`/
  `KnowledgeGraph` presentation layer, no real local LLM binding) - all
  unchanged, see the Phase 7 session block below for detail.

## Next recommended task (as of end of Phase 8 session)

Phase 9 - Import/Export (CSV, JSON, encrypted backup, restore) per
`ROADMAP.md` - **not started**, awaiting explicit owner review/
direction per the roadmap's "stop for review before the next phase"
policy (re-confirmed, not overridden, this session).

## Completed in Phase 7 session (Local AI)

- Implemented Phase 7 per ROADMAP.md: `LocalAiPort` (local-model
  abstraction) with `RuleBasedLocalAiEngine` as its default,
  deterministic, fully-offline binding - explicitly confirmed with the
  user before implementation, since no model weights can be downloaded
  in this offline environment and a real ONNX Runtime/llama.cpp
  integration was judged too large and too risky to complete reliably
  in one session. New `domain/.../ai/` package: `AiAnswer`/`AiCitation`,
  `AiIntent`/`AiIntentClassifier`, `FinancialRepositories`/
  `FinancialSnapshot`, `AiMoneyFormatter`, `ai/usecase/`
  (`BuildFinancialSnapshotUseCase`, `RetrieveFinancialContextUseCase`,
  `AskAiAssistantUseCase`), `ai/engine/` (`RuleBasedLocalAiEngine` + 6
  responder objects, one per intent, each reusing an existing Phase
  2/3/4 calculator or port). No schema/`:data` changes - read-only
  phase. New `composeApp/.../ai/` package (`AiAssistantScreen` +
  `AiAssistantScreenBody` + `AiCitationRow`) as a 5th bottom-nav
  destination, "Assistant". Full detail in `05-current-state.md` and
  `15-ai-runtime.md` (rewritten from a 2-line stub).
- Scope decisions made explicit, all confirmed via an `AskUserQuestion`
  before coding began (see `15-ai-runtime.md`): (1) no real local LLM -
  a deterministic rule engine behind a swappable port instead; (2)
  semantic retrieval is lexical (reuses Phase 4's FTS5 `SearchPort`),
  not vector-embedding-based, for the same "no downloadable model"
  reason; (3) `AiMoneyFormatter` duplicates `composeApp`'s
  `MoneyFormatter` rather than having `:domain` depend on Presentation.
- The build gate took 3 attempts to go green: (1) ~20 ktlint
  argument-list-wrapping/parameter-list-wrapping/chain-method-continuation
  violations across `BudgetResponder.kt`, `SpendingResponder.kt`,
  `RetrieveFinancialContextUseCase.kt`, `RuleBasedLocalAiEngineTest.kt`,
  and an import-ordering violation in `AppModule.kt` - fixed in one
  shot with `./gradlew ktlintFormat` (auto-fix) instead of hand-editing
  each violation, which was much faster than the manual-fix approach
  used in earlier phases; (2) one detekt `MaxLineLength` on a KDoc
  comment in `CashFlowResponder.kt` (ktlint's formatter does not
  rewrap comments), fixed by shortening the sentence; (3)
  `BUILD SUCCESSFUL in 1m 35s`, 395 tasks - confirmed via the literal
  log text, and cross-checked that all 14 new AI test cases (6
  classifier + 3 retrieval + 5 engine) show `failures="0" errors="0"`
  in both `desktopTest` and Android `testDebugUnitTest` result XML.
- Lesson reinforced again this session: when a `--continue` Gradle run
  fails only on ktlint style violations (not detekt/compile errors),
  `./gradlew ktlintFormat` auto-fixes them in one pass far faster than
  manually rewriting each wrapped argument list by hand - worth trying
  first before hand-editing, reserving manual fixes for detekt/compile
  issues the formatter can't touch.
- Every new file is under 300 lines (largest:
  `RuleBasedLocalAiEngineTest.kt`, ~190 lines).
- Updated memory bank: `02-system-architecture.md`, `03-domain-model.md`,
  `04-roadmap.md`, `05-current-state.md`, `06-tech-stack.md`,
  `07-repository-structure.md`, `14-search-engine.md`,
  `15-ai-runtime.md` (fleshed out from a 2-line stub),
  `18-ui-design-system.md`, `26-active-initiatives.md`, this file.

## Not completed (as of end of Phase 7 session)

- No real local LLM or embedding model is integrated - `LocalAiPort`
  has only the deterministic `RuleBasedLocalAiEngine` binding.
- `AiIntentClassifier` has no fuzzy matching, synonym handling, or
  confidence scoring - a fixed English keyword list.
- `BudgetResponder` still cannot report over/under-budget status - the
  underlying spend-to-date tracking gap predates Phase 7 (Phase 2).
- The AI conversation history is session-only (not persisted) and does
  not create `MemoryEvent`s (Phase 6) even though the two features are
  conceptually related.
- Same Phase 1/3/5/6 items as before (Android instrumented test,
  `androidx.security.crypto` deprecation, Desktop key-file hardening,
  CVD palette validation, OCR/`DocumentPicker` device verification,
  `Relationship`/`KnowledgeGraph` presentation layer) - all unchanged,
  see the Phase 6 session block below for detail.

## Next recommended task (as of end of Phase 7 session)

Phase 8 - Security (biometrics, auto-lock, backup encryption, audit
log) per `ROADMAP.md` - **not started**, awaiting explicit owner
review/direction per the roadmap's "stop for review before the next
phase" policy (re-confirmed, not overridden, this session).

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
