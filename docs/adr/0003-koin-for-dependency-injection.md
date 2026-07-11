# ADR 0003: Koin for Dependency Injection

Status: Accepted
Date: 2026-07-11

## Context

ROADMAP.md Phase 0 requires establishing dependency injection.
SystemPrompt Part 2 requires module communication through interfaces,
events, use cases, and dependency injection - never static utilities -
and Part 1 lists "Explicit Dependencies" as a required engineering
practice. ADR 0002 commits the project to Kotlin Multiplatform
(Android + Desktop).

## Problem

Which DI approach fits a Kotlin Multiplatform (Android + JVM desktop)
codebase without weakening the Clean Architecture boundary?

## Options Considered

1. Hilt - the common Android choice, but it is Android-only
   (annotation processor tied to the Android Gradle Plugin); it cannot
   wire the desktop target.
2. Manual constructor injection with hand-written factories - zero
   dependencies, fully explicit, but becomes unwieldy once the
   composition root wires many features across two platform entry
   points.
3. Koin - a lightweight, reflection-free-at-runtime (uses a DSL, not
   annotation processing), Kotlin-Multiplatform-native DI library.

## Decision

Use Koin, restricted to the composition root.

- `:domain` never depends on Koin. Its classes take their dependencies
  as constructor parameters, as plain interfaces - this preserves
  "Domain Layer... No framework code."
- `:data` provides a Koin module (`dataModule`) that binds each domain
  repository interface to its concrete implementation.
- `:composeApp` provides a Koin module (`appModule`) that wires domain
  use cases from the bindings `:data` provides, and calls `startKoin`
  once per platform entry point (`OpfisApplication` on Android, `main`
  on desktop).

## Consequences

- Only `:data` and `:composeApp` take a Koin dependency; `:domain` and
  `:shared` stay framework-free and testable with plain constructor
  injection in unit tests (no DI container needed in tests).
- Two `startKoin` call sites (Android `Application.onCreate`, desktop
  `main`) must be kept in sync as modules are added; this is a known,
  accepted cost of supporting two platform entry points.

## Alternatives Rejected

- Hilt: rejected outright - does not support the desktop target
  required by ADR 0002.
- Manual factories only: rejected as a Phase 0 decision because the
  composition root will grow substantially from Phase 2 onward
  (financial engine, search engine, document engine, AI runtime all
  need wiring); Koin's module DSL keeps that growth manageable without
  reopening this decision every phase. Manual construction remains the
  norm *inside* `:domain` and `:data` - Koin is only used at the
  composition root and for repository binding, not throughout the
  codebase.

## Follow-up Actions

- None at this time. Revisit only if Koin's runtime service-locator
  model becomes a measured performance or testability problem.
