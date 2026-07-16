# Backup Engine

Encrypted export/import, user-controlled backups (ROADMAP Phase 9,
"Import/Export": CSV, JSON, encrypted backup, restore). Builds on
Phase 1's `BackupPort` primitive, which Phase 8 deliberately left
unwrapped ("no UI to call it from yet").

## Three portability formats, one screen

`composeApp/.../importexport/ImportExportScreen` offers three
independent export/import pairs - they solve different problems, not
three versions of the same thing:

1. **Encrypted backup** (`ExportBackupUseCase`/`RestoreBackupUseCase`
   wrapping `BackupPort`) - a byte-for-byte copy of the live SQLCipher
   database via SQLite's `VACUUM INTO`. Opaque, but the most complete
   and the only format safe to treat as a full disaster-recovery
   snapshot (every table, including `audit_log`).
2. **JSON** (`ExportFinancialDataUseCase`/`ImportFinancialDataUseCase`)
   - every user-owned financial entity in one human-readable,
   portable file. The intended "move my data to a new install"
   format. Excludes `AuditLogEntry` (security history isn't "financial
   data") and never inlines `Document` file bytes (metadata + extracted
   text only - see `16-document-engine.md`).
3. **CSV** (`ExportTransactionsCsvUseCase`/`ImportTransactionsCsvUseCase`)
   - transactions only, for spreadsheets. Every other entity type
   (accounts, categories, budgets, tags, ...) has no natural single
   tabular shape, so CSV was deliberately not extended to them -
   confirmed with the owner during planning.

## Why restoring a backup requires an app restart

`FileBackupPort.restoreBackup` (both `androidMain`/`desktopMain`
actuals) closes its own `SqlDriver` before copying the backup file over
the live database file. This is not optional: on Windows, an open file
handle blocks overwriting; on every platform, a live `SqlDriver`
connection must never be left pointing at a file that was just swapped
out from under it. But every Koin-held `OpfisDatabase`/repository
singleton is permanently bound to that one driver instance at
composition-root startup - there is no supported way to "reopen" the
driver in place and have all downstream repositories pick up the new
connection without reconstructing the entire DI graph, which was judged
too large and risky to attempt in one session. So the UI's restore flow
is: confirm (destructive, explicit dialog) -> stage the picked file to a
temp path -> `RestoreBackupUseCase` -> on success, terminate the
process (`composeApp/.../io/AppExit`) with a message telling the user to
reopen the app. This is the same trade-off many consumer apps make for
restore flows, not a shortcut unique to this codebase.

## Android's `content://` Uri problem

Android's `ActivityResultContracts.CreateDocument`/`GetContent` SAF
contracts hand back `content://` Uris, not real filesystem paths - but
`VACUUM INTO` (export) and `File.copyTo` (restore, inside
`FileBackupPort`) both need a real path. Both platforms always stage
through a temp file (`composeApp/.../io/TempFile`) before/after talking
to the user-chosen SAF destination, even though Desktop's `FileDialog`
could technically hand back a usable path directly - trading a small,
one-time extra temp-file copy on Desktop for one identical code path
across both platforms in `ImportExportScreen`, rather than branching UI
logic per platform.

## Data safety notes

- JSON/CSV import writes are `id`-preserving upsert/`INSERT OR REPLACE`
  (transactions go through `FinancialLedgerPort.recordTransaction`,
  never a raw upsert, so account balances stay consistent) - re-
  importing the same export file is therefore idempotent, not
  duplicate-creating.
- Every action (all three formats, both directions) is recorded to the
  Phase 8 audit log: `BACKUP_EXPORTED`/`BACKUP_RESTORED` for the
  encrypted-backup flow, `DATA_EXPORTED`/`DATA_IMPORTED` for JSON/CSV -
  visible on the Security screen's audit trail.
- `RelationshipRepository` gained `observeAll()` this phase (it
  previously only supported `observeInvolving(entityType, entityId)`)
  purely so full-dataset JSON export has a way to enumerate every
  relationship - the one port that had no existing "read everything"
  primitive.

## Known gaps

- Not build-verified this session (no JDK/Android SDK available) - see
  `05-current-state.md`/`06-tech-stack.md`.
- No progress indicator for large exports/imports - `ImportExportScreen`
  shows only a final status message, not an in-flight spinner/percentage.
- No dry-run/preview before import - a JSON/CSV import applies
  immediately; there is no "here's what would change" confirmation step
  (the encrypted-backup restore is the only flow with a confirmation
  dialog, because it alone is destructive).
