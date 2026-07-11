# Repository Structure

```
composeApp/   Presentation layer + composition root
  src/commonMain/   Shared Compose UI (App.kt, theme/, systemstatus/, di/AppModule.kt)
  src/androidMain/  MainActivity, OpfisApplication (Koin startKoin), AndroidManifest.xml
  src/desktopMain/  Main.kt (application entry point, Koin startKoin)

domain/       Domain + Application layers (pure Kotlin, no framework deps)
  src/commonMain/kotlin/com/opfis/domain/<feature>/            entities, repository interfaces
  src/commonMain/kotlin/com/opfis/domain/<feature>/usecase/    use cases
  src/commonTest/                                               unit tests (fakes, no DI container)

data/         Infrastructure layer
  src/commonMain/kotlin/com/opfis/data/<feature>/   repository implementations
  src/commonMain/kotlin/com/opfis/data/di/          Koin module (dataModule)

shared/       Cross-cutting kernel, zero dependencies
  src/commonMain/   ports (e.g. logging/Logger.kt with `expect fun platformLogger()`)
  src/androidMain/  Android actuals
  src/desktopMain/  Desktop actuals

docs/adr/                    Architecture Decision Records (0001, 0002, 0003 so far)
config/detekt/detekt.yml     Static analysis rules
.github/workflows/ci.yml     CI (lint, detekt, test, assemble)
project-memory-bank/         Engineering memory bank - read this first
SystemPrompt/                Engineering operating system (5 parts)
```

Module ownership: each Gradle module owns its own package namespace
(`com.opfis.shared`, `com.opfis.domain`, `com.opfis.data`,
`com.opfis.app`). Feature slices (e.g. `systemstatus`) get one package
per module they touch, named identically across modules, so the
vertical slice is easy to find (e.g. `domain/.../systemstatus/`,
`data/.../systemstatus/`, `composeApp/.../systemstatus/`).
