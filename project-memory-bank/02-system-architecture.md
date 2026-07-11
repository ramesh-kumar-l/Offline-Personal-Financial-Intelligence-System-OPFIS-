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
