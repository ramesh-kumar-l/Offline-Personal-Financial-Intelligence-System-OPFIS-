# Current State

Last updated: 2026-07-11 (Phase 0 closed - build verified green; Phase 1
Core Persistence implemented and tested)

## Implemented

### Phase 0 - Foundation (closed, exit criteria met)

- Repository scaffold: 4 Gradle modules (`:shared`, `:domain`, `:data`,
  `:composeApp`) per ADR 0001, Kotlin Multiplatform + Compose
  Multiplatform per ADR 0002, Koin DI wiring per ADR 0003.
- Real toolchain verified on 2026-07-11: JDK 25.0.3, Android SDK
  (platform 36 / API 36.1 extension 20, build-tools 37.0.0), Gradle
  9.6.1 (wrapper jar generated and committed), AGP 9.2.1 with
  compatibility flags (ADR 0004), Kotlin 2.4.0, Compose Multiplatform
  1.11.1, Koin 4.2.2 - all bumped from Phase 0's originally-guessed
  versions and verified against Maven Central/Google Maven.
- `./gradlew ktlintCheck detekt allTests assemble` passes for both
  Android (`:composeApp:assembleDebug`) and Desktop targets.
- 5 ADRs recorded (`docs/adr/0001`-`0005`), indexed in
  `24-adr-index.md`.

### Phase 1 - Core Persistence (implemented and tested)

- Encrypted database: SQLDelight 2.3.2 + SQLCipher (ADR 0005).
  `OpfisDatabase` (`com.opfis.data.db`) with a `system_status_indicator`
  table (id, label, state, created_at, updated_at, version - matching
  SystemPrompt Part 2's per-table audit metadata requirement).
- Platform driver factories (`DatabaseDriverFactory` expect/actual,
  `:data`): Android uses `AndroidSqliteDriver` +
  `net.zetetic:android-database-sqlcipher`'s `SupportFactory`; Desktop
  uses `JdbcSqliteDriver` + `io.github.willena:sqlite-jdbc` in
  SQLCipher-4-compatible mode (`SQLiteMCSqlCipherConfig.getV4Defaults()`).
- Encryption key provider (`DatabaseKeyProvider` expect/actual):
  Android via `androidx.security.crypto` `EncryptedSharedPreferences`
  (Keystore-backed); Desktop via a random key written once to a file in
  `~/.opfis` (Phase 8 hardens this - see ADR 0005 follow-up).
- `PersistentSystemStatusRepository` replaces Phase 0's static
  `LocalSystemStatusRepository`: seeds default trust indicators once,
  observes them via a SQLDelight `Flow`. `encrypted_storage` now
  reports ACTIVE (was PENDING in Phase 0) because the encryption is
  real.
- Migration system: SQLDelight versioned `.sqm` migrations
  (`data/src/commonMain/sqldelight/migrations/1.sqm`), auto-applied by
  the schema-aware driver factory function on open.
- Backup interfaces: `BackupPort` (`:domain`), implemented by
  `FileBackupPort` (`:data`, Android and Desktop) using SQLite
  `VACUUM INTO` for consistent export and a file copy for restore.
  Full CSV/JSON import-export and restore UX remain Phase 9 scope.
- Tests (all real, no mocks, run via `:data:desktopTest` against the
  actual SQLCipher-backed driver): data survives close/reopen with the
  same passphrase; a wrong passphrase cannot read previously written
  data; a v1 (pre-migration) database auto-migrates to the current
  schema on open with data intact; backup export/restore round-trips
  data through `FileBackupPort`. All 7 new tests pass.

## Known gaps / not yet verified

- Android's encrypted driver path (`AndroidSqliteDriver` +
  `SupportFactory`) compiles and `:composeApp:assembleDebug` succeeds,
  but there is no Android emulator/device in this environment, so the
  actual on-device encrypted read/write behavior is not exercised by an
  instrumented test - only Desktop's equivalent path is (ADR 0005
  follow-up).
- `androidx.security.crypto`'s `EncryptedSharedPreferences`/`MasterKey`
  are flagged deprecated by the library itself as of the version used
  (1.1.0, the latest stable on Maven Central as of 2026-07-11) - revisit
  the replacement API when Phase 8 designs biometric/auto-lock gating.
- Desktop's encryption key file (`~/.opfis/.opfis_db_key`) has no
  OS-keychain protection yet - documented, intentional Phase 1 scope
  boundary (ADR 0005), not an oversight.
- AGP 9's `android.builtInKotlin=false`/`android.newDsl=false`
  compatibility flags (ADR 0004) are a deprecated-but-working path;
  must be revisited before any AGP 10.0 upgrade.

## Pending

- Phase 2 onward (see `04-roadmap.md` / `ROADMAP.md`), starting with
  Phase 2 (Financial Domain: accounts, assets, liabilities,
  transactions, categories, budgets, goals) - not started, pending
  explicit approval per the phase-execution policy.
