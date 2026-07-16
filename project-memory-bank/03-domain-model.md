# Domain Model

Phase 2 entities live under `domain/src/commonMain/kotlin/com/opfis/domain/`,
one package per entity (`<entity>/`, ports in the same package, use
cases in `<entity>/usecase/`). All monetary fields are `Long` minor
units (e.g. cents) - no floating point - and Phase 2 has no multi-currency
support (single implicit currency; deferred, not designed yet).

- **Account** (`account/`) - `id, name, type, balanceMinorUnits,
  isArchived, createdAt, updatedAt`. `balanceMinorUnits` is never edited
  directly outside of `FinancialLedgerPort` once transactions exist.
- **Asset** (`asset/`) - `id, name, type, valueMinorUnits, createdAt,
  updatedAt`. Independent of Account; a manually-tracked valuation
  (stocks, gold, real estate, EPF/PPF/NPS - PRD).
- **Liability** (`liability/`) - `id, name, type, balanceMinorUnits,
  interestRateBasisPoints?, createdAt, updatedAt`. Also independent of
  Account in Phase 2.
- **Category** (`category/`) - `id, name, type (INCOME/EXPENSE),
  parentId?, createdAt, updatedAt`. Self-referential for one level (or
  more) of nesting; a category cannot be its own parent (enforced in
  `init`).
- **Transaction** (`transaction/`) - `id, accountId, categoryId?, type
  (INCOME/EXPENSE/TRANSFER), amountMinorUnits (> 0), transferAccountId?,
  description, occurredAt, createdAt, updatedAt`. Validates TRANSFER has
  a distinct `transferAccountId`; non-transfers must not have one.
- **Budget** (`budget/`) - `id, categoryId, limitMinorUnits (> 0),
  period (WEEKLY/MONTHLY/YEARLY), startDate, createdAt, updatedAt`. Only
  the definition; spend-to-date computation is Dashboard/analytics work
  (ROADMAP Phase 3+).
- **Goal** (`goal/`) - `id, name, targetAmountMinorUnits (> 0),
  currentAmountMinorUnits (>= 0), targetDate?, createdAt, updatedAt`.
  `currentAmountMinorUnits` is a user-facing progress snapshot, edited
  directly via upsert (unlike Account's ledger-derived balance).

## The financial engine (see `12-financial-engine.md`)

`TransactionLedgerRules` (`domain/transaction/`) is a pure, framework-free
domain policy object translating a `Transaction` into signed per-account
balance deltas (`accountDeltas`) and their inverse (`reversalDeltas`).
`FinancialLedgerPort` is the write-side port (`recordTransaction`,
`deleteTransaction`); `TransactionRepository` is the read-side port
(`observeAll`, `observeByAccount`). Splitting reads from the
ledger-mutating write path means a caller can never bypass the
atomic balance-adjustment contract.

## Out of Phase 2 scope

`User`, `Document`, `MemoryEvent`, `Tag`, `Institution`, `Backup`,
`Settings`, `AuditLog` appear in the PRD's full data model but are not
part of ROADMAP Phase 2 ("Financial Domain": Accounts, Assets,
Liabilities, Transactions, Categories, Budgets, Goals) and are not
implemented yet.

## Phase 3 derived models (read-only, not persisted)

These are computed from Phase 2 entities on the fly - none are new
database tables:

- **NetWorthSummary** (`networth/`) - `accountBalanceMinorUnits,
  assetValueMinorUnits, liabilityBalanceMinorUnits`, with computed
  `totalAssetsMinorUnits`/`netWorthMinorUnits`. Built by
  `NetWorthCalculator.calculate(accounts, assets, liabilities)`,
  excluding archived accounts.
- **CashFlowPeriod** (`cashflow/`) - `year, month, incomeMinorUnits,
  expenseMinorUnits`, with computed `netMinorUnits`. Built by
  `CashFlowCalculator.summarizeByMonth(...)`, excluding TRANSFER
  transactions; months with no activity still appear with zero totals.
- **SearchResult** (`search/`) - sealed class: `AccountMatch`,
  `CategoryMatch`, `TransactionMatch`. Built by
  `FinancialSearchEngine.search(...)`, a deliberately minimal
  substring match (see `14-search-engine.md` - Phase 4 replaces this
  with FTS5).

No historical net-worth snapshots exist, so there is deliberately no
"net worth trend" chart in Phase 3 (would require fabricating data
SystemPrompt Part 3 forbids this).

## Phase 4 additions

- **Tag** (`tag/`) - `id, name, colorHex?, createdAt, updatedAt`.
  Assigned to transactions via a many-to-many `transaction_tag` join
  table (`TransactionTagRepository`: assign/unassign/observeForTransaction).
  Client-generated id via `kotlin.uuid.Uuid.random()` (first use of a
  UUID-based id strategy in this project - see `02-system-architecture.md`).
- **SearchResult** gained `TagMatch`; `SearchFilter` (`entityTypes`,
  `tagIds`, `occurredFrom`/`occurredTo`) narrows a `SearchPort` query or
  the new **TimelineEntry** (`timeline/`) chronological browse - see
  `14-search-engine.md` for the FTS5 mechanics and the deliberate
  tag/date-filter scope cut (timeline-only, not global text search).

## Phase 5 additions

- **Document** (`document/`) - `id, fileName, storagePath, mimeType,
  documentType (RECEIPT/STATEMENT/INVOICE/OTHER), extractedText,
  linkedTransactionId?, importedAt, createdAt, updatedAt`. `init`
  requires `fileName`/`storagePath`/`mimeType` non-blank. The row never
  holds file bytes - `storagePath` points at a file the platform
  `DocumentStoragePort` owns. Optionally linked to one `Transaction`
  (the "receipt vault" use case); `extractedText` (OCR/PDF-extracted)
  feeds Phase 4's `search_index` FTS5 table so documents are globally
  searchable - see `14-search-engine.md`, `16-document-engine.md`.
- **SearchResult** gained `DocumentMatch`.

## Phase 6 additions

- **EntityType** / **EntityRef** (`entity/`) - a cross-cutting entity
  kind enum + `(entityType, entityId)` pointer used by `MemoryEvent` and
  `Relationship` so neither package needs to depend on every feature
  package directly. Deliberately separate from `search/SearchEntityType`
  (Phase 4), which only lists FTS5-indexed kinds.
- **MemoryEvent** (`memory/`) - `id, eventType (NOTE/MILESTONE), title,
  description, subject: EntityRef?, occurredAt, createdAt, updatedAt`.
  `init` requires a non-blank `title`. Manually recorded only - see
  `13-memory-engine.md` for the scope decision not to auto-generate
  these from other use cases this phase. Feeds `search_index` (title +
  description), so `SearchResult` gained `MemoryEventMatch`.
- **Relationship** (`relationship/`) - `id, from: EntityRef, to:
  EntityRef, relationshipType (RELATED/SUPPORTING_DOCUMENT/
  CONTRIBUTES_TO/PART_OF), createdAt, updatedAt`. `init` requires
  `from != to`. Stores only links the schema has no dedicated FK column
  for - `Document.linkedTransactionId` and `transaction_tag` remain
  each feature's own concern, not duplicated here.
- **KnowledgeGraph** (`relationship/`, derived, not persisted) -
  `root: EntityRef, neighbors: List<EntityRef>, edges: List<Relationship>`.
  Built by the pure `KnowledgeGraphBuilder.build(root, relationships)`
  from a root entity's `Relationship`s (1-hop only).

## Phase 7 additions (read-only, not persisted)

- **AiAnswer** / **AiCitation** (`ai/`) - `AiAnswer(text,
  citations: List<AiCitation>)`; `AiCitation(entityType: EntityType,
  entityId, label)` reuses Phase 6's `EntityType` so an answer can point
  back to any addressable record.
- **FinancialSnapshot** (`ai/`, derived, not persisted) - a one-shot
  in-memory bundle of every Account/Asset/Liability/Transaction/
  Category/Budget/Goal, built by `BuildFinancialSnapshotUseCase`
  through the `FinancialRepositories` holder (7 repositories bundled
  into one constructor parameter, same detekt `LongParameterList`
  pattern as Phase 5's `ImportDocumentRequest`).
- **RetrievedItem** (`ai/`) - `entityType, entityId, summary`; produced
  by `RetrieveFinancialContextUseCase` wrapping Phase 4's `SearchPort`
  for lexical (not vector) semantic retrieval - see `15-ai-runtime.md`.

See `15-ai-runtime.md` for `LocalAiPort`, `AiIntentClassifier`, and the
per-intent responder objects that compose `AiAnswer`s.

## Phase 8 additions

- **AuditLogEntry** (`audit/`) - `id, eventType, description,
  occurredAt`. Append-only: `AuditLogRepository` exposes `record`/
  `observeAll` only, no update/delete - an audit trail must stay
  trustworthy.
- **AuditEventType** (`audit/`) - `APP_UNLOCKED, APP_UNLOCK_FAILED,
  BACKUP_EXPORTED, BACKUP_RESTORED`. The two `BACKUP_*` values have no
  producer yet - they're ready for Phase 9's backup/restore UI to
  record directly.
- **AutoLockPolicy** (`security/`, pure, not an entity) - a stateless
  idle-timeout check (`shouldLock(lastInteractionAt, now, timeout)`);
  the actual lock state lives in `composeApp`'s `AppLockState`, not
  `:domain`.
- No new persisted financial-domain entities this phase - Phase 8 is a
  cross-cutting security layer over the existing data, not new
  financial data itself.

## Phase 9 additions

- **AuditEventType** (`audit/`) gained `DATA_EXPORTED`, `DATA_IMPORTED`
  (JSON/CSV flows) alongside the existing `BACKUP_*` pair (encrypted
  whole-DB flow, unused until this phase).
- **FinancialDataSnapshot** (`importexport/`, derived, not persisted) -
  a full-dataset export/import unit bundling every entity list
  (accounts, assets, liabilities, categories, transactions, budgets,
  goals, tags, `transactionTagAssignments: List<TransactionTagAssignment>`,
  documents, memoryEvents, relationships) plus `schemaVersion`/
  `exportedAtEpochMillis`. Every domain entity + enum involved gained a
  bare `@Serializable` annotation (kotlinx.serialization, first use in
  this project) rather than a parallel DTO hierarchy. `AuditLogEntry`
  is deliberately excluded - audit history isn't "financial data" to
  round-trip. `TransactionTagAssignment(transactionId, tagId)` stands
  in for the join table, which has no domain entity of its own.
- **ImportExportCoreRepositories** / **ImportExportRelatedRepositories**
  (`importexport/`) - two repository-bundle data classes (7 + 6 fields)
  so `ExportFinancialDataUseCase`/`ImportFinancialDataUseCase` stay
  under detekt's `LongParameterList` threshold, the same technique as
  Phase 7's `FinancialRepositories`.
- **ImportSummary** (`importexport/usecase/`) - `countsByEntity: Map<String, Int>`,
  the per-entity-type row counts an import replayed, for UI feedback. A
  map rather than one field per entity, again to stay under the
  parameter-count threshold.
- `RelationshipRepository` gained `observeAll()` (previously only
  `observeInvolving(entityType, entityId)`) - a direct, narrowly
  justified port extension since full-dataset export has no other way
  to enumerate every relationship.
- CSV (`TransactionCsvCodec`, `importexport/`) is scoped to
  `Transaction` only - the one entity with a natural tabular shape.
  Columns mirror `Transaction`'s own fields 1:1 (id-based, not
  human-readable account/category names) so import never needs an
  ambiguous name -> id lookup.
