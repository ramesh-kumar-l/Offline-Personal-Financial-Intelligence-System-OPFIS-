# Tech Stack

Kotlin Multiplatform + Compose Multiplatform (ADR 0002), targeting
Android and Desktop (JVM) from `:composeApp`. Android-only APIs
(Keystore, WorkManager, biometrics) are accessed via `expect`/`actual`
per platform, never called directly from `commonMain`.

Verified against a real JDK 25 + Android SDK 36 toolchain and Maven
Central/Google Maven on 2026-07-11 (`./gradlew ktlintCheck detekt
allTests assemble` all green - see `05-current-state.md`). Re-verified
2026-07-17 (Phase 11) on JDK 21 + Android SDK 36 - same build gate
green, now also covering Phases 9 and 10's previously-unverified code
plus Phase 11's own new tests and the new
`org.jetbrains.compose.ui:ui-test` dependency (version-matched to
`compose-multiplatform` 1.11.1, `composeApp`'s new `desktopTest`
source set).

- Kotlin 2.4.0
- Compose Multiplatform 1.11.1
- Android Gradle Plugin 9.2.1, minSdk 26, compileSdk/targetSdk 36.
  Built on the legacy `com.android.library`/`com.android.application` +
  KMP plugin combination via `android.builtInKotlin=false` /
  `android.newDsl=false` (ADR 0004) - not the new
  `com.android.kotlin.multiplatform.library` plugin.
- Gradle 9.6.1 (wrapper jar committed; JDK 25 runs it without issue)
- Koin 4.2.2 for dependency injection (ADR 0003), used only at the
  composition root (`:data`, `:composeApp`) - `:domain` stays
  framework-free
- kotlinx.coroutines 1.11.0
- ktlint (org.jlleitschuh.gradle.ktlint 14.2.0) + detekt 1.23.8 for
  coding standards. Compose-generated resource accessor code under
  `build/generated/` is exempted from ktlint via `.editorconfig`
  (`ktlint = disabled` for `**/generated/**/*.kt`), and `@Composable`
  functions are exempted from both tools' function-naming rules.
- SQLDelight 2.3.2 + SQLCipher (`net.zetetic:android-database-sqlcipher`
  on Android, `io.github.willena:sqlite-jdbc` on Desktop) for encrypted
  persistence (ADR 0005, Phase 1)
- SQLite FTS5 (Phase 4), OCR (Phase 5): integrated. Embedded vector
  search and a real local LLM runtime (ONNX Runtime/llama.cpp): still
  not integrated after Phase 7 - no model weights can be downloaded in
  this offline environment, so Phase 7 shipped `LocalAiPort` (the
  abstraction) with a deterministic rule-engine default binding
  instead. See `15-ai-runtime.md`.
- `androidx.biometric:biometric` 1.1.0 (Phase 8, androidMain only) for
  real device biometric/device-credential authentication -
  `androidx.biometric.BiometricPrompt` needs a `FragmentActivity`, so
  `MainActivity` now extends `FragmentActivity` instead of
  `ComponentActivity`. No JVM-side biometric API exists for Desktop
  (documented gap, see `09-security-model.md`). Desktop's SQLCipher key
  file gained owner-only file permissions/ACL hardening in Phase 8, but
  full OS keychain/DPAPI integration (ADR 0005's original Phase 8
  follow-up) is still not implemented.
- `kotlinx-serialization-json` 1.8.0 + the
  `org.jetbrains.kotlin.plugin.serialization` compiler plugin (Phase 9,
  `:domain` only) - first use of kotlinx.serialization in this project,
  for `FinancialDataSnapshot`'s JSON export/import. Version picked from
  general knowledge, not confirmed against Maven Central's real
  metadata (unlike the `androidx.biometric` precedent in Phase 8) - see
  the Phase 9 caveat below.

**Phase 9 was implemented in an environment with no JDK, no Android
SDK, and no prior Gradle cache** (confirmed: no `java`/`javac` on
`PATH`, no JDK under `Program Files`/`.jdks`/registry, no `~/.gradle`).
Unlike every prior phase, `./gradlew ktlintCheck detekt allTests
assemble` could **not** be run - Phase 9's code is manually
self-reviewed (types, imports, detekt parameter-count thresholds,
expect/actual signatures) but not compiler- or test-verified. Running
the full build gate on a machine with the real toolchain is the first
thing the next session (or the user) must do before trusting Phase 9
as "green" - see `05-current-state.md` and `30-session-handoff.md`.
