# Tech Stack

Kotlin Multiplatform + Compose Multiplatform (ADR 0002), targeting
Android and Desktop (JVM) from `:composeApp`. Android-only APIs
(Keystore, WorkManager, biometrics) are accessed via `expect`/`actual`
per platform, never called directly from `commonMain`.

Verified against a real JDK 25 + Android SDK 36 toolchain and Maven
Central/Google Maven on 2026-07-11 (`./gradlew ktlintCheck detekt
allTests assemble` all green - see `05-current-state.md`).

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
- SQLite FTS5, embedded vector search, local LLM runtime (ONNX Runtime
  abstraction), OCR: planned, not yet integrated (Phase 4 / Phase 5 /
  Phase 7)
