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
