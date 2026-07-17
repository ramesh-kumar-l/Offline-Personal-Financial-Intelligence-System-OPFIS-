# ADR Collection

Two sections. Section A summarizes the 5 ADRs that exist and are
ratified in `docs/adr/` today (read those files for full text — this
is a condensed index, not a replacement). Section B proposes 5
additional ADRs for decisions the project has already made in
practice (visible in `project-memory-bank/`) but never formalized as a
standalone ADR. Section B is explicitly **proposed, not ratified** —
nothing there should be read as an existing project decision record.

## Section A — Existing, Ratified ADRs

### ADR 0001: Clean Architecture with a Lean Module Boundary
**Decision**: 4 Gradle modules (`:shared`, `:domain`, `:data`,
`:composeApp`), dependencies flowing inward only, enforced by the
build graph.
**Alternatives**: 11+ pre-created empty modules (rejected — premature
structure, violates YAGNI); single package-layered module (rejected —
boundary not enforceable by the compiler).
**Consequences**: `:domain` cannot physically import Compose/Koin/SQL
types. `:domain` currently combines Domain+Application layers as one
module, flagged for revisit once it grows past its current size.

### ADR 0002: Compose Multiplatform as the Presentation Target
**Decision**: Kotlin Multiplatform + Compose Multiplatform,
`androidTarget()` + `jvm("desktop")` from the start; iOS out of scope.
**Alternatives**: Android-only (rejected — would force a full
Presentation-layer rewrite later to add Desktop, contradicting the
project's stated Longevity pillar).
**Consequences**: Android-only APIs (Keystore, biometric prompts) must
be `expect`/`actual`, not called directly from shared code.

### ADR 0003: Koin for Dependency Injection
**Decision**: Koin, restricted to the composition root (`:data`,
`:composeApp` only); `:domain`/`:shared` stay framework-free.
**Alternatives**: Hilt (rejected — Android-only, incompatible with the
Desktop target); manual factories only (rejected as a Phase 0
decision — judged unwieldy once the composition root grows past
Phase 2).
**Consequences**: Two `startKoin` call sites (Android, Desktop) must
be kept in sync as modules are added.

### ADR 0004: AGP 9 Compatibility Flags for KMP + Android Application Modules
**Decision**: `android.builtInKotlin=false` / `android.newDsl=false`
in `gradle.properties`, keeping the legacy `com.android.application` +
KMP plugin combination rather than migrating to
`com.android.kotlin.multiplatform.library` immediately.
**Alternatives**: migrate now (rejected — real module-boundary
restructuring, open Compose-preview bugs on the new plugin as of
AGP 9.2); pin back to AGP 8.x (rejected — trades one unverified
compatibility surface for another).
**Consequences**: explicitly time-boxed — "this ADR's decision has a
shelf life," must be revisited before any AGP 10.0 upgrade.

### ADR 0005: SQLDelight + SQLCipher for Persistence
**Decision**: SQLDelight 2.3.2 on both platforms; Android via
`net.zetetic:android-database-sqlcipher`'s `SupportFactory`, Desktop
via `io.github.willena:sqlite-jdbc` (with `org.xerial:sqlite-jdbc`
excluded to avoid a `DriverManager` prefix collision).
**Alternatives**: Room KMP (rejected — no established Desktop
SQLCipher support); plain SQLite + field-level encryption (rejected —
breaks FTS5 and general queryability); deferring Desktop encryption to
a later phase (rejected — fails the phase's own "persistent encrypted
storage" exit criterion).
**Consequences**: two structurally different encryption-key strategies
per platform — Android's is Keystore-backed, Desktop's is a
file-based key explicitly documented as "the acknowledged weak point
of this ADR," with OS keychain/DPAPI integration tracked as real
follow-up work.

## Section B — Proposed, Not Yet Ratified

These formalize decisions already visible in the codebase and
`project-memory-bank/` but never written up as a standalone ADR. They
are proposals for what *should* be added to `docs/adr/`, not existing
records — presented here to demonstrate the same decision-quality
discipline extends past the five formally logged so far.

### Proposed ADR 0006: Deterministic Rule-Based Engine Before a Local LLM
**Decision** (as practiced): ship `LocalAiPort`'s v1.0 implementation
as `RuleBasedLocalAiEngine` — deterministic, source-citing, zero
network calls — rather than waiting for a bundled neural model.
**Alternatives**: defer the `Assistant` screen entirely until a real
local LLM/embedding runtime (ONNX Runtime, llama.cpp) is integrated;
call a cloud LLM API (rejected outright — violates the offline-first
non-goal).
**Rationale**: no model weights could be downloaded in this
development environment; a rule-based engine satisfies the *harder*
requirement (explainable, cited answers) immediately, and the port
abstraction means swapping in a real model later doesn't change any
caller.
**Consequences**: "local AI" in current marketing copy must be
described precisely (rule-based, not neural) to avoid a credibility
gap with a technical audience — see `documents/04`'s honesty
constraint.

### Proposed ADR 0007: Whole-Database Encrypted Backup Over Selective Export
**Decision** (as practiced): the primary backup/restore mechanism
copies the entire SQLCipher-encrypted database file (`BackupPort`),
rather than a selective per-entity export.
**Alternatives**: a fully custom backup format with per-entity
selection (rejected — duplicates the migration/versioning problem
SQLDelight already solves for the live schema); cloud backup
(rejected — contradicts the offline-first, no-mandatory-cloud
principle).
**Rationale**: the live schema is already versioned and
migration-tested; reusing it for backup avoids maintaining a second,
parallel data format.
**Consequences**: restore is necessarily whole-database and
destructive (requires closing the app), and is tied to the same
device's key material — not yet a portable, passphrase-based export
across devices; tracked as future import/export UX work.

### Proposed ADR 0008: SQLite FTS5 Over an Embedded Vector Index for v1 Search
**Decision** (as practiced): Phase 4 search ships on SQLite FTS5
(lexical full-text search) rather than an embedded vector/semantic
index, despite VISION.md §13 naming "embedded vector index" as part
of the technology vision.
**Alternatives**: build/integrate a local embedding model + vector
index immediately (rejected for v1 — no local embedding runtime is
integrated yet, same constraint as Proposed ADR 0006).
**Rationale**: FTS5 delivers real, immediate, correct search over
every entity type with no new runtime dependency; semantic search is
an additive upgrade once a local embedding model exists, not a
blocker to shipping v1 search.
**Consequences**: search today is lexical (keyword-based), not
semantic (won't match paraphrases) — an explicit, documented v1
limitation, not a silent gap.

### Proposed ADR 0009: Manual-Confirm Fallback Over a Third-Party Desktop Biometric Library
**Decision** (as practiced): the Desktop `Security` screen uses an
explicit "Confirm to unlock" manual gate rather than integrating a
third-party OS-biometric bridge library for the JVM.
**Alternatives**: integrate a native bridge to Windows Hello/
platform-specific biometric APIs (rejected — no single, maintained,
cross-OS-uniform JVM biometric API exists; would mean three separate
native integrations for one screen).
**Rationale**: correctness and auditability (every unlock still
produces a real `APP_UNLOCKED` audit-log entry) matter more than
biometric parity with Android for v1; the fallback is documented
behavior, not an unhandled gap.
**Consequences**: Desktop security is gated by app-level confirmation,
not OS-level biometric hardware, which is weaker than Android's real
`BiometricPrompt` path — an accepted, documented platform asymmetry.

### Proposed ADR 0010: Hand-Written Fakes Over a Mocking Framework in Domain Tests
**Decision** (as practiced): every `:domain` unit test uses a
hand-written `Fake*Repository` implementing the real repository
interface, rather than a mocking framework (Mockito/MockK).
**Alternatives**: a mocking framework (rejected — adds a dependency
and reflection/proxy behavior to a layer whose entire point is to stay
framework-free and trivially constructible; also encourages testing
interaction details rather than real behavior).
**Rationale**: `:domain` interfaces are already small and
purpose-built as ports; a fake is often less code than the equivalent
mock setup, and it exercises the real interface contract instead of a
stub's assumptions about it.
**Consequences**: adding a method to a repository interface requires
updating every fake that implements it — an accepted, deliberate cost
in exchange for tests that can't silently drift from the real
interface shape.
