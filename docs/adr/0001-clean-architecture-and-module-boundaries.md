# ADR 0001: Clean Architecture with a Lean Module Boundary

Status: Accepted
Date: 2026-07-11

## Context

SYSTEM.md and SystemPrompt Part 2 mandate strict Clean Architecture
(Presentation -> Application -> Domain -> Infrastructure, dependencies
pointing inward only) and Domain-Driven Design. The architecture doc
also lists a large eventual module set (core, financial-engine,
memory-engine, search-engine, document-engine, security, analytics,
ai, backup, settings, ui, shared). Phase 0 only needs to establish the
boundary, not every future module.

## Problem

How granular should the Gradle module structure be at Phase 0, given
YAGNI and "never partially implement future phases," while still
making the Clean Architecture boundary real (enforced by the build
graph, not just convention)?

## Options Considered

1. One Gradle module per architecture layer name listed in Part 2
   (11+ modules), mostly empty at Phase 0.
2. Single module with package-based layering (`com.opfis.presentation`,
   `.application`, `.domain`, `.infrastructure`), deferring module
   splits indefinitely.
3. A lean four-module split that enforces the dependency direction at
   the build-graph level, growing feature modules per phase as real
   code arrives.

## Decision

Option 3. Phase 0 introduces exactly four Gradle modules:

- `:shared` - zero-dependency kernel. Cross-cutting abstractions with
  no business logic (e.g. the `Logger` port). Depends on nothing.
- `:domain` - Domain and Application layers combined in one module,
  separated by package (`com.opfis.domain.model`,
  `com.opfis.domain.<feature>` for entities/ports,
  `com.opfis.domain.<feature>.usecase` for use cases). Depends only on
  `:shared` (for abstractions, never implementations). No framework,
  Android, or SQL code is permitted here.
- `:data` - Infrastructure layer. Implements domain repository
  interfaces, owns platform adapters. Depends on `:domain` and
  `:shared`.
- `:composeApp` - Presentation layer and composition root. A Kotlin
  Multiplatform module targeting `androidTarget()` and `jvm("desktop")`
  via Compose Multiplatform. Depends on `:domain`, `:data`, and
  `:shared`. Owns Koin startup for both platform entry points.

Application and Domain share one module for now because splitting
them into separate Gradle modules today would produce two modules with
almost no code and no build-graph benefit (nothing yet needs the
Application layer to be independently swappable from Domain). This
will be revisited once Phase 2 (Financial Domain) adds enough use
cases and entities that the module is doing double duty.

## Consequences

- The dependency direction (Presentation -> Data -> Domain -> Shared,
  and Domain -> Shared) is enforced by Gradle's dependency graph, not
  just code review discipline: `:domain` physically cannot import
  Compose, Koin, or SQL types because those artifacts are not on its
  classpath.
- Feature modules (financial-engine, search-engine, document-engine,
  etc.) are expected to arrive as new Gradle modules in later phases,
  each depending on `:domain`/`:shared` and consumed by `:composeApp`,
  rather than being pre-created empty now.
- Splitting `:domain` into separate `:domain` and `:application`
  modules remains an open option; tracked as a follow-up, not a
  present requirement.

## Alternatives Rejected

- Full multi-module (Option 1): rejected as premature structure with
  no code to justify the module boundaries yet (violates YAGNI).
- Single module (Option 2): rejected because it does not make the
  architecture boundary buildable/enforceable - a Presentation-layer
  file could import SQLCipher directly and nothing would stop it.

## Follow-up Actions

- Revisit the Domain/Application split when Phase 2 introduces the
  first real financial use cases.
- Add feature modules (e.g. `:financial-engine`) as their phases
  begin, following the same dependency rules established here.
