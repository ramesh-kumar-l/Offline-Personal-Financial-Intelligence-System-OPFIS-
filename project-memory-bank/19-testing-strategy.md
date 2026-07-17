# Testing

## Phase 11 - Testing (implemented 2026-07-17)

Full build gate (`ktlintCheck detekt allTests assemble`, Android +
Desktop) is **green** on a real JDK 21 + Android SDK 36 toolchain -
see `05-current-state.md`/`06-tech-stack.md`. This closes the
build-verification gap left open by Phase 9/10 (no JDK was available
in that session's environment).

### Unit tests (`:domain`, `commonTest`)

Every entity/policy object (`TransactionLedgerRules`,
`NetWorthCalculator`, `CashFlowCalculator`, `AutoLockPolicy`,
`KnowledgeGraphBuilder`, ...) and Application-layer use case has a
test. Each test file defines its own `private class FakeXRepository`
implementing the port under test (no shared/mocking-framework fakes,
matching the established convention) - `kotlin.test` +
`kotlinx-coroutines-test` only, no mockk/turbine added. This phase
closed a large pre-existing gap: most simple CRUD use cases
(account/asset/budget/category/liability/goal/tag/memory/relationship/
document/cashflow/ai) had zero tests before this phase - only the
"showcase" flows (net worth, timeline, search, knowledge graph,
backup, import/export) were covered. New consolidated `XUseCasesTest`
files (one per feature, not one per use case) keep the file count
proportionate to the low risk of these thin delegators.

**Naming caution**: `private class FakeXRepository` is file-scoped,
but two files in the *same package* declaring the identical class name
still collide at compile time (found the hard way with
`FakeDocumentRepository` and `FakeRelationshipRepository` - both
already existed in their packages under different test files). Check
existing fakes in a package before naming a new one.

### Integration tests (`:data`, `desktopTest`)

Every `Sql<Entity>Repository` runs against a real in-memory-shaped
SQLite database via the `testDatabase()` helper (`TestDatabase.kt`) -
no mocks. `SchemaMigrationTest` proves the full v1-to-current migration
chain applies cleanly with data intact. New this phase:
`SqlAuditLogRepositoryTest` (the one `Sql*Repository` that had no test
- security-relevant, since it's the audit trail's actual persistence).

### UI tests (`:composeApp`, `desktopTest`) - new this phase

`:composeApp` had **zero** tests of any kind before this phase (no
`androidTest`, no `desktopTest`, no UI test dependency declared).
Added `org.jetbrains.compose.ui:ui-test` (version-matched to the
project's Compose Multiplatform 1.11.1) to a new `desktopTest` source
set, using the headless `runComposeUiTest` API (no Robolectric, no
Android instrumentation needed - JVM/Desktop-only for now).
`LockScreenBodyTest` is the first example, chosen because
`LockScreenBody` is pure/stateless (no Koin injection) and
security-critical (the app's lock gate) - it verifies the Unlock tap,
the auth-failed message, and the biometric-unavailable manual-confirm
fallback all render/wire correctly. Screens that need Koin (most of
the other 44 composeApp files) are NOT yet covered - see "Known gaps"
below.

### Security tests

`AutoLockPolicyTest` (idle-timeout policy), `AuditLogUseCasesTest` +
`SqlAuditLogRepositoryTest` (record/observe, append-only, newest-first
ordering), `EncryptedPersistenceRecoveryTest` (SQLCipher round-trip,
wrong-passphrase rejection, backup export/restore file round-trip).
This phase also **corrected a wrong assumption** one of these tests
was asserting: `restoreBackup closes the driver so it cannot be used
afterward` asserted that querying a closed `SqlDriver` throws - it
doesn't, on Desktop's `io.github.willena:sqlite-jdbc` driver, which
transparently reopens a connection on next use. The test was rewritten
to assert the invariant that's actually true and actually matters (the
reopened connection reads the restored file, not stale pre-restore
data); `FileBackupPort`'s doc comments and `17-backup-engine.md` were
corrected to match. `LockScreenBodyTest` also covers the
auth-failed/biometric-unavailable UI states.

### Performance benchmarks

No automated benchmark harness (no `androidx.benchmark` or JMH added -
would be new infrastructure for a single-developer offline app, judged
disproportionate this phase). `20-performance-budget.md`'s three named
targets (cold start <1s, search <100ms, dashboard <300ms) are
structurally targeted (Phase 10's indexed queries/pre-warm/reduced
polling) but still require a manual timed run on a real device/desktop
to confirm - see that file's "Verification status" section, updated
this phase now that a real build exists to actually run.

### Known gaps

- **UI test coverage is minimal**: one screen (`LockScreenBody`) out
  of ~10 screens. The other screens (`Dashboard`, `Search`, `Vault`,
  `Memory`, `AiAssistant`, `Security`, `ImportExportScreen`) all
  Koin-inject use cases directly in their top-level `Composable`, so
  testing them needs either a test Koin module or further splitting
  state/wiring from layout (most already follow the `XScreen` +
  `XScreenBody` split - the `XScreenBody` composables are the natural
  next candidates, same pattern as this phase's `LockScreenBody`).
- **No Android instrumented tests** - no emulator/device in this
  environment (recurring gap since Phase 1). The encrypted driver path,
  biometric auth, OCR, and `DocumentPicker`/`FileSaver` Android actuals
  remain unverified on a real device.
- **No automated performance benchmarks** - budgets are targeted, not
  measured; see `20-performance-budget.md`.
- **No mutation testing or property-based testing** - out of scope,
  not requested.
