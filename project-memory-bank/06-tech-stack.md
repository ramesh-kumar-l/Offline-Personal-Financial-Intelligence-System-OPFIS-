# Tech Stack

Kotlin Multiplatform + Compose Multiplatform (ADR 0002), targeting
Android and Desktop (JVM) from `:composeApp`. Android-only APIs
(Keystore, WorkManager, biometrics) are accessed via `expect`/`actual`
per platform, never called directly from `commonMain`.

- Kotlin 2.1.0
- Compose Multiplatform 1.7.1
- Android Gradle Plugin 8.7.2, minSdk 26, compileSdk/targetSdk 35
- Koin 4.0.0 for dependency injection (ADR 0003), used only at the
  composition root (`:data`, `:composeApp`) - `:domain` stays
  framework-free
- kotlinx.coroutines 1.9.0
- ktlint (org.jlleitschuh.gradle.ktlint) + detekt for coding standards
- SQLCipher, SQLite FTS5, embedded vector search, local LLM runtime
  (ONNX Runtime abstraction), OCR: planned, not yet integrated (Phase
  1 / Phase 5 / Phase 7)

Exact dependency versions were chosen for currency as of 2026-07-11
but have not been resolved against Maven Central from this
environment (no internet access in the build sandbox) - verify on
first real `./gradlew` sync and bump if newer patch/minor releases
exist.
