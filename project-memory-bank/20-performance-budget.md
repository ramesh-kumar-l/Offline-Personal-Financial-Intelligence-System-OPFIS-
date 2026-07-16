# Performance
Cold start <1s, search <100ms, dashboard <300ms.

## Phase 10 work toward these budgets (2026-07-16)

Targeted, narrowly-justified changes only - see `02-system-architecture.md`
for the full list. In support of the three budgets above:

- **Dashboard <300ms**: `ObserveRecentTransactionsUseCase` (the
  Dashboard's "Recent Activity" widget) used to call
  `TransactionRepository.observeAll()` and sort/take in Kotlin - an
  unbounded full-table load re-run on every emission. It now calls a
  new `observeRecent(limit)`, backed by an indexed
  `SELECT ... ORDER BY occurred_at DESC LIMIT :limit` query
  (`migrations/7.sqm` adds the `occurred_at` index). This is the
  session's highest-confidence win, since it directly shrinks both
  query cost and in-app memory on the busiest read path.
- **Cold start <1s**: `Main.kt`/`OpfisApplication.kt` now pre-warm the
  `OpfisDatabase` Koin singleton on a background `Dispatchers.IO`
  coroutine immediately after `startKoin`, instead of letting the first
  screen's `koinInject()` call open the encrypted driver (SQLCipher key
  derivation + schema migration) synchronously on the Compose
  UI/composition thread. The app shows the lock screen first, so this
  overlaps the warm-up with the time the user spends unlocking.
- **Battery** (not one of the three named budgets, but ROADMAP Phase
  10 also lists it): `App.kt`'s auto-lock idle-check poll dropped from
  every 1s to every 15s - still far below the 5-minute
  `AutoLockPolicy` timeout, ~15x fewer background wakeups.
- **Query optimization (general)**: added the missing
  `financial_transaction_transfer_account_id` index, supporting
  `selectByAccount`'s existing `WHERE account_id = ? OR
  transfer_account_id = ?` (only the `account_id` side was indexed
  before).

## What was deliberately not done

- **SQLite `WAL` + `synchronous=NORMAL` pragmas** were considered and
  rejected for this pass: Phase 9's `restoreBackup` closes the driver
  then overwrites the main `.db` file with `File.copyTo`, but does not
  clean up `-wal`/`-shm` sidecar files. Enabling WAL introduces a real,
  untested interaction risk with that just-built restore path (a stale
  WAL file replayed against a freshly-restored main file could corrupt
  it) that cannot be verified without a real build/runtime in this
  environment. Deferred rather than shipped unverified - revisit
  together with a WAL-aware restore (checkpoint + delete sidecars
  before copy) in a future session.
- No index was added for `budget.category_id` or `category.parent_id`
  - no existing query filters on either column (both tables are only
  ever read via `selectAll`/`selectById`), so an index there would be
  speculative, unused weight.
- No pagination was added for the audit log or JSON export/import
  (both already-known gaps, see `05-current-state.md`) - out of scope
  for this pass; the app's realistic data scale (a single user's
  personal finances) doesn't yet justify the added complexity.

## Verification status

**Not empirically measured.** This session's environment has no
JDK/Android SDK (same constraint documented for Phase 9 in
`06-tech-stack.md`), so there is no way to run `./gradlew assemble`,
profile a cold start, or time a search/dashboard render. All four
changes above are structurally justified (indexed bounded queries,
moved blocking I/O off the main thread, reduced poll frequency) and
manually reviewed, but "performance budgets achieved" cannot be
confirmed as an exit criterion until someone runs the app on a real
device/desktop and measures cold start, search latency, and dashboard
render time against the targets at the top of this file.
