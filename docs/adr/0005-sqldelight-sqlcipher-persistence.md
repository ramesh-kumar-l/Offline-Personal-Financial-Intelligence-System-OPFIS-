# ADR 0005: SQLDelight + SQLCipher for Phase 1 Persistence

Status: Accepted
Date: 2026-07-11

## Context

SystemPrompt Part 2 ("Storage Architecture", "Database Principles")
mandates SQLCipher as the single, encrypted, transactional source of
truth - no cloud database, no Firebase/Realm, nothing important stored
outside it. ROADMAP.md Phase 1 ("Core Persistence") requires: SQLCipher,
schema, a repository layer, migrations, and backup interfaces, exiting
on "persistent encrypted storage" and "recovery tests".

ADR 0002 committed the project to Kotlin Multiplatform with Android and
Desktop (JVM) targets. Any persistence choice must work, encrypted, on
both. Plain `android.database.sqlite` is Android-only. Room's KMP
support does not extend to Desktop with SQLCipher today. SQLDelight
(`app.cash.sqldelight`) is the established KMP-first option: it
generates typed Kotlin from `.sq` files, runs on any JDBC- or
platform-driver-backed target, and ships a first-class versioned
migration mechanism (`.sqm` files + `verifySqlDelightMigration`), which
directly satisfies the "Migrations" deliverable.

SQLDelight itself is driver-agnostic about encryption - encryption is a
property of which driver/native library sits underneath it:

- **Android**: `app.cash.sqldelight:android-driver`'s `AndroidSqliteDriver`
  accepts a `SupportSQLiteOpenHelper.Factory`.
  `net.zetetic:android-database-sqlcipher` provides exactly that
  (`SupportFactory(passphrase)`), giving a real SQLCipher-encrypted
  database under the standard SQLDelight API.
- **Desktop**: `app.cash.sqldelight:sqlite-driver`'s `JdbcSqliteDriver`
  opens whatever JDBC driver is registered for the `jdbc:sqlite:` URL
  prefix via `java.sql.DriverManager`. SQLDelight's own dependency
  transitively pulls in `org.xerial:sqlite-jdbc`, which does **not**
  support SQLCipher. `io.github.willena:sqlite-jdbc` is a drop-in,
  actively maintained fork that registers the same URL prefix and adds
  `PRAGMA key`/SQLCipher support. Because `DriverManager` picks the
  first driver registered for a given prefix, `org.xerial:sqlite-jdbc`
  must be excluded from the desktop classpath so `willena`'s driver is
  the only one that can claim `jdbc:sqlite:`.

SQLDelight formerly shipped a purpose-built `sqlcipher-driver` artifact;
it was removed from the 2.x line (confirmed absent from Maven Central
for 2.3.2), so the `SupportFactory`-based approach above is the current
supported path, not a workaround.

## Decision

Use **SQLDelight 2.3.2** for schema/query generation and migrations,
with:

- **Android**: `AndroidSqliteDriver` + `SupportFactory` from
  `net.zetetic:android-database-sqlcipher:4.5.4`.
- **Desktop**: `JdbcSqliteDriver` + `io.github.willena:sqlite-jdbc:3.50.1.0`,
  with `org.xerial:sqlite-jdbc` excluded from
  `app.cash.sqldelight:sqlite-driver`'s transitive dependencies in
  `data/build.gradle.kts`.

Both driver factories live in `:data` (Infrastructure layer) as
`expect class DatabaseDriverFactory` / `actual class` per platform,
following the same pattern ADR 0002 established for `:shared`'s
`Logger`. The database itself is named `OpfisDatabase`
(`com.opfis.data.db` package), and Phase 1's schema is scoped to what
Phase 1 actually needs: a `system_status_indicator` table that upgrades
the Phase 0 in-memory `LocalSystemStatusRepository` to real encrypted
persistence, proving the full CRUD + migration + recovery path without
inventing Phase 2's financial-domain tables early.

### Encryption key source (Phase 1 scope, hardened in Phase 8)

ROADMAP.md scopes biometrics and hardened key storage to Phase 8
("Security"), separately from Phase 1 ("Core Persistence"). Phase 1
therefore needs *a* real per-install encryption key, not yet a
biometric-gated one:

- **Android**: the key is generated once and stored via
  `androidx.security.crypto:security-crypto:1.1.0`'s
  `EncryptedSharedPreferences`, which is itself backed by Android
  Keystore (`MasterKey`). This is real device-backed protection, not a
  placeholder - Phase 8 adds biometric/auto-lock gating on top of it,
  it does not replace it.
- **Desktop**: there is no OS-uniform equivalent available today. The
  key is a randomly generated 256-bit value written once to a file
  inside the app's private data directory. This is the acknowledged
  weak point of this ADR: a local attacker with filesystem access to
  that directory can read the key. Hardening this (OS
  keychain/DPAPI integration) is explicit Phase 8 follow-up work, not
  silently deferred - it is the direct Desktop equivalent of Keystore
  and belongs in the same phase.

## Consequences

- `:data` gains real Android/Desktop-only dependencies
  (`net.zetetic:android-database-sqlcipher`, `io.github.willena:sqlite-jdbc`,
  `androidx.security.crypto`), each confined to its platform source set.
- `LocalSystemStatusRepository` (Phase 0's static in-memory stand-in) is
  replaced by `PersistentSystemStatusRepository`, backed by the
  encrypted database, rather than kept alongside it as dead code.
- Composition root wiring changes: `:data`'s `dataModule` (commonMain)
  can no longer construct `DatabaseDriverFactory` itself (it needs a
  platform-specific `Context` or file path it doesn't have in
  `commonMain`). Each platform now supplies a small
  `androidDataModule` / `desktopDataModule` Koin module providing the
  platform `DatabaseDriverFactory` and `DatabaseKeyProvider`, loaded
  alongside `dataModule` from `OpfisApplication.kt` (Android) and
  `Main.kt` (Desktop).
- Desktop database encryption cannot be verified by this Phase - there
  is no Android emulator/device in this environment - Android's actual
  `AndroidSqliteDriver`/`SupportFactory` wiring compiles and is unit
  tested with fakes, but its encrypted-at-rest behavior is not
  exercised by an instrumented test here. Desktop's driver, by
  contrast, runs directly on the JVM and *is* exercised end-to-end by
  real `:data` desktop tests (wrong-key rejection, reopen-after-close,
  backup/restore round-trip).

## Alternatives Rejected

- **Room (KMP)**: rejected - Desktop-target SQLCipher support is not
  established the way it is for SQLDelight's JDBC driver model.
- **Plain unencrypted SQLite + application-level field encryption**:
  rejected - contradicts Part 2's explicit "no cloud database... never
  spread critical data across multiple storage systems" and "everything
  important belongs inside SQLCipher" principles; column-level
  encryption also breaks `FTS5` (Phase 4) and general queryability.
- **Defer Desktop encryption to Phase 8, ship Desktop unencrypted for
  now**: rejected - Phase 1's exit criterion is explicitly "persistent
  *encrypted* storage"; shipping one target unencrypted would fail that
  criterion for half the product's platforms.

## Follow-up Actions

- Phase 8: replace Desktop's file-based key with OS keychain/DPAPI
  integration; add biometric/auto-lock gating on top of Android's
  Keystore-backed key on both platforms.
- Phase 2: extend `OpfisDatabase`'s schema with financial-domain tables
  (accounts, transactions, etc.) using the same `.sq` + migration
  pattern established here.
- When an Android emulator/device becomes available, add an
  instrumented test exercising the real `AndroidSqliteDriver` +
  `SupportFactory` path end-to-end.
