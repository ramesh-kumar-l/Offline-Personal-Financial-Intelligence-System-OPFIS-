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
