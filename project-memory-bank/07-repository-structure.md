# Repository Structure

```
composeApp/   Presentation layer + composition root
  src/commonMain/   Shared Compose UI (App.kt, theme/, dashboard/, format/, di/AppModule.kt)
  src/androidMain/  MainActivity, OpfisApplication (Koin startKoin), AndroidManifest.xml
  src/desktopMain/  Main.kt (application entry point, Koin startKoin)

domain/       Domain + Application layers (pure Kotlin, no framework deps)
  src/commonMain/kotlin/com/opfis/domain/<feature>/            entities, repository interfaces
  src/commonMain/kotlin/com/opfis/domain/<feature>/usecase/    use cases
  src/commonMain/kotlin/com/opfis/domain/backup/                BackupPort (Phase 1)
  src/commonMain/kotlin/com/opfis/domain/transaction/            TransactionLedgerRules, FinancialLedgerPort (Phase 2)
  src/commonMain/kotlin/com/opfis/domain/{account,asset,liability,category,budget,goal}/  Phase 2 entities/ports/usecases
  src/commonMain/kotlin/com/opfis/domain/{networth,cashflow,search}/  Phase 3 derived-model calculators/engine + usecase/
  src/commonTest/                                               unit tests (fakes, no DI container)

data/         Infrastructure layer
  src/commonMain/kotlin/com/opfis/data/<feature>/   repository implementations (Sql<Entity>Repository, SqlFinancialLedger)
  src/commonMain/kotlin/com/opfis/data/db/          DatabaseDriverFactory/DatabaseKeyProvider (expect), OpfisDatabase
  src/commonMain/kotlin/com/opfis/data/di/          Koin module (dataModule)
  src/commonMain/sqldelight/                        .sq schema (Account/Asset/Liability/Category/Budget/Goal/
                                                     FinancialTransaction, Phase 2) + migrations/*.sqm
  src/androidMain/kotlin/com/opfis/data/            SQLCipher/EncryptedSharedPreferences actuals, androidDataModule
  src/desktopMain/kotlin/com/opfis/data/            SQLite3MultipleCiphers actuals, desktopDataModule
  src/desktopTest/kotlin/com/opfis/data/            real (non-mocked) encrypted DB tests, incl. SqlFinancialLedgerTest

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
`com.opfis.app`). Feature slices get one package per module they touch,
named identically across modules, so the vertical slice is easy to find
(e.g. `domain/.../account/`, `data/.../account/`).

`composeApp/.../dashboard/` (Phase 3) is presentation-only - it has no
`:data` counterpart since Phase 3 introduced zero schema/repository
changes, only new `:domain` use cases combining existing repositories.
`composeApp/.../format/` holds locale-API-free formatting helpers
(`MoneyFormatter`, `MonthLabelFormatter`, `DateFormatter`). The Phase 0
`systemstatus/` package (composeApp) was retired in Phase 3 - its
`SystemStatusScreen` was deleted and its trust-indicator concept folded
into `dashboard/TrustIndicatorsSection.kt`.

Phase 4 adds: `domain/.../{tag,timeline}/` (+ `tag/usecase/`,
`timeline/usecase/`) and `domain/.../search/{SearchFilter,SearchPort}.kt`;
`data/.../{tag,search}/` (`SqlTagRepository`,
`SqlTransactionTagRepository`, `SqlSearchIndexRepository`,
`FtsQueryBuilder`) plus extracted `data/.../{account,category}/
<Entity>Mapper.kt` top-level mapper functions; `data/.../db/` schema
gained `Tag.sq`/`TransactionTag.sq`/`SearchIndex.sq` and
`migrations/3.sqm`; `composeApp/.../search/` (`SearchScreen` +
`SearchScreenBody` + filter bar/tag chips/results list/timeline
section) and a bottom `NavigationBar` in `App.kt`.

Phase 5 adds: `domain/.../document/` (+ `document/usecase/`) - entity,
`DocumentRepository`/`DocumentStoragePort`/`DocumentTextExtractorPort`
ports, 5 use cases; `data/.../document/` (`SqlDocumentRepository`,
`DocumentMapper` in `commonMain`; `DesktopDocumentStorage`,
`DesktopDocumentTextExtractor`, `TesseractEngine` in `desktopMain`;
`AndroidDocumentStorage`, `AndroidDocumentTextExtractor` in
`androidMain`) plus `data/.../db/` schema gaining `Document.sq` and
`migrations/4.sqm`; `composeApp/.../document/` (`DocumentVaultScreen` +
`DocumentVaultScreenBody` + `DocumentRow`, `DocumentPicker`
`expect`/`actual` in `commonMain`/`desktopMain`/`androidMain`) and a
third `NavigationBar` destination ("Vault") in `App.kt`.

Phase 6 adds: `domain/.../entity/` (`EntityType`, `EntityRef`),
`domain/.../memory/` (+ `memory/usecase/`), `domain/.../relationship/`
(+ `relationship/usecase/`, `KnowledgeGraph.kt`); `data/.../memory/`
(`SqlMemoryEventRepository`, `MemoryEventMapper`), `data/.../relationship/`
(`SqlRelationshipRepository`, `RelationshipMapper`) plus `data/.../db/`
schema gaining `MemoryEvent.sq`/`Relationship.sq` and `migrations/5.sqm`;
`composeApp/.../memory/` (`MemoryScreen` + `MemoryScreenBody` +
`MemoryEventRow`) and a fourth `NavigationBar` destination ("Memory")
in `App.kt`.
