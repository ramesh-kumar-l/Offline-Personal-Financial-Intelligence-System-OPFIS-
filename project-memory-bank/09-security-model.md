# Security

Encryption, Android Keystore, biometrics, least privilege, no telemetry by default.

## Phase 8 - Security (implemented)

See `05-current-state.md` for the full implementation writeup and
`02-system-architecture.md` for how it fits the module graph. Summary:

- **Encryption at rest** (Phase 1, ADR 0005, unchanged): SQLCipher via
  SQLDelight on both targets. Android's per-install key is stored in
  `EncryptedSharedPreferences` (Android Keystore-backed). Desktop's key
  is a randomly generated file, now (Phase 8) restricted to the owning
  OS account - POSIX `rw-------` where supported, or a best-effort
  Windows ACL (single owner-only `AclEntry`) + hidden attribute
  otherwise. This narrows, but does not close, ADR 0005's flagged weak
  point: full OS keychain/DPAPI integration is still not implemented.
- **Biometrics**: `composeApp/.../security/BiometricAuth.kt`
  (`@Composable expect fun rememberBiometricAuthLauncher`) - a
  presentation-layer capability, not a `:domain` port, because
  `androidx.biometric.BiometricPrompt` needs a live `FragmentActivity`
  that a Koin-injected domain port has no way to obtain (same reasoning
  as Phase 5's `DocumentPicker`). Android's actual checks
  `BiometricManager.canAuthenticate` (biometric-weak or
  device-credential) before showing `BiometricPrompt`; `MainActivity`
  was changed from `ComponentActivity` to `FragmentActivity` to host
  it. Desktop's actual always resolves to `NotAvailable` - there is no
  OS-uniform biometric API on the JVM.
- **Auto-lock**: `domain/.../security/AutoLockPolicy.kt` is a pure,
  stateless idle-timeout check (default 5 minutes). `AppLockState`
  (`composeApp`, `@Stable`) owns the actual lock state: the app starts
  locked; tapping a bottom-nav destination counts as "interaction" and
  re-arms the timer, checked once a second. This is a deliberately
  simpler interaction signal than tracking raw touch/scroll events
  across all six screens, which would need a global pointer-event
  interceptor risking interference with existing gesture handling
  (scrolling lists, chart canvases, forms) for uncertain benefit - a
  scope decision, not an oversight.
- **Backup encryption**: interpreted as hardening the key that
  `BackupPort`'s exports (Phase 1, `VACUUM INTO` on an already-keyed
  SQLCipher connection) inherit their encryption from, per ADR 0005's
  own stated Phase 8 follow-up - not as building Phase 9's portable,
  passphrase-based backup/restore UX, which ROADMAP.md scopes
  separately to Phase 9 ("Encrypted backup"). `ExportBackupUseCase`/
  `RestoreBackupUseCase` were deliberately not added this phase since
  there is no backup UI yet to call them from; adding unwired use cases
  would violate CLAUDE.md's "no speculative code" guidance.
- **Audit log**: `domain/.../audit/` (`AuditLogEntry`/`AuditEventType`,
  `AuditLogRepository`, `RecordAuditEventUseCase`,
  `ObserveAuditLogUseCase`), persisted to a new `audit_log` table
  (schema v7, `migrations/6.sqm`), append-only (no update/delete).
  Records `APP_UNLOCKED`/`APP_UNLOCK_FAILED` on every unlock attempt
  (`LockScreen`), including Desktop's manual "Confirm to unlock"
  fallback (distinctly worded from a biometric unlock, so the trail
  stays honest about how the app was actually unlocked).
  `BACKUP_EXPORTED`/`BACKUP_RESTORED` exist in the enum with no
  producer yet, ready for Phase 9 to record directly. No retention
  policy or pruning UI exists - the log grows unbounded.
- Presentation: a 6th bottom-nav destination, "Security"
  (`SecurityScreen` + `SecurityScreenBody` + `AuditLogRow`) - a policy
  summary card above the audit trail list. `App.kt` wraps its entire
  content in the lock gate (`LockScreen` while `AppLockState.isLocked`).

## Known gaps (all documented, none silent)

- No real biometric authentication on Desktop (JVM has no OS-uniform
  API) - manual confirmation fallback only.
- No full OS keychain/DPAPI integration for Desktop's database key -
  file-permission/ACL hardening only.
- Auto-lock's interaction signal is bottom-nav navigation, not
  comprehensive touch/scroll tracking.
- No portable, passphrase-based encrypted backup/restore yet (Phase
  9's job) - today's backup inherits the live database's device-bound
  key, so a restored backup only works on the same device/Keystore.
- Audit log has no retention policy, export, or pruning UI.
- No `PRAGMA foreign_keys` enforcement (Phase 2 gap, unchanged).
- Android's encrypted driver path and OCR/biometric paths remain
  unverified against a real device/emulator - no Android runtime
  available in this environment (recurring caveat since Phase 1).
