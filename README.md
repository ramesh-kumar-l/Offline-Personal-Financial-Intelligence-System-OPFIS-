# OPFIS - Offline Personal Financial Intelligence System

Privacy-first, offline-first, AI-native personal financial
intelligence platform. See [VISION.md](VISION.md), [PRD.md](PRD.md),
and [ROADMAP.md](ROADMAP.md) for the product definition, and
[SYSTEM.md](SYSTEM.md) plus [SystemPrompt/](SystemPrompt/) for the
engineering operating system this repository is built under.

## Status

**Phase 0 - Foundation** (see `project-memory-bank/05-current-state.md`
for the current, up-to-date status). This is a scaffold: the
architecture boundary, module structure, and dependency injection
wiring are in place; no financial domain logic exists yet.

## Architecture

Clean Architecture + DDD, four Gradle modules:

- `:shared` - zero-dependency kernel (e.g. the `Logger` port).
- `:domain` - Domain + Application layers (entities, repository
  interfaces, use cases). No framework or platform dependencies.
- `:data` - Infrastructure layer (repository implementations, Koin
  bindings).
- `:composeApp` - Presentation layer and composition root. Kotlin
  Multiplatform (Compose Multiplatform), targeting Android and
  Desktop.

See `docs/adr/` for the reasoning behind these decisions.

## Toolchain setup

This scaffold was authored without a working local build - verify it
compiles as your first step. You need:

1. **JDK 17+**
2. **Android SDK** with `platform;android-35` and a recent
   `build-tools` version, plus `cmdline-tools` (for `sdkmanager`).
   Point `ANDROID_HOME`/`ANDROID_SDK_ROOT` at it, or create
   `local.properties` at the repo root with:
   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   ```
3. **The Gradle wrapper jar.** `gradle/wrapper/gradle-wrapper.jar` is
   a binary and is not committed yet. Generate it once, from any
   machine with a JDK and any Gradle install (or Android Studio, which
   does this automatically on first project open):
   ```sh
   gradle wrapper --gradle-version 8.10
   ```
   After that, use `./gradlew` (or `gradlew.bat` on Windows) as usual.

Then verify:

```sh
./gradlew ktlintCheck detekt allTests assemble
```

## Repository layout

```
composeApp/   Presentation layer + composition root (Android, Desktop)
domain/       Domain + Application layers (pure Kotlin)
data/         Infrastructure layer
shared/       Cross-cutting kernel (no business logic)
docs/adr/     Architecture Decision Records
project-memory-bank/   Engineering memory bank (read this first)
```
