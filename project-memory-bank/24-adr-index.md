# ADR Index

Record all architecture decisions. Full text in `docs/adr/`.

- 0001 - Clean Architecture with a lean module boundary (`:shared`,
  `:domain`, `:data`, `:composeApp`; Domain+Application share one
  module for now)
- 0002 - Compose Multiplatform as the Presentation target (Android +
  Desktop from Phase 0; supersedes the Android-only entry that was
  previously in `06-tech-stack.md`)
- 0003 - Koin for dependency injection, restricted to `:data` and
  `:composeApp`; `:domain`/`:shared` stay framework-free
- 0004 - AGP 9 compatibility flags (`android.builtInKotlin=false`,
  `android.newDsl=false`) to keep all four modules on the
  `com.android.library`/`application` + KMP plugin combination instead
  of migrating to `com.android.kotlin.multiplatform.library`
- 0005 - SQLDelight 2.3.2 + SQLCipher for Phase 1 persistence
  (`net.zetetic:android-database-sqlcipher` on Android,
  `io.github.willena:sqlite-jdbc` on Desktop); Android key stored via
  `androidx.security.crypto` (Keystore-backed), Desktop key via a
  private-directory file pending Phase 8 hardening
