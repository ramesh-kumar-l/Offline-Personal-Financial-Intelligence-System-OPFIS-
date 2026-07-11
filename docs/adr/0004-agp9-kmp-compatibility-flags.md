# ADR 0004: AGP 9 Compatibility Flags for KMP + Android Application Modules

Status: Accepted
Date: 2026-07-11

## Context

Phase 0 was scaffolded without a working local toolchain (documented in
`05-current-state.md` as an open gap). The owner has since installed
JDK 25 and a current Android SDK (platform 36, API level 36.1 /
extension 20, build-tools 37.0.0). With real internet access available
in this environment, the dependency versions pinned during the
toolchain-less scaffolding phase were verified against Maven Central /
Google Maven and bumped to the current stable releases as of
2026-07-11: Gradle 9.6.1, AGP 9.2.1, Kotlin 2.4.0, Compose Multiplatform
1.11.1, Koin 4.2.2.

Attempting the first real build against AGP 9.2.1 failed immediately:

```
An exception occurred applying plugin request [id: 'com.android.application', version: '9.2.1']
> Failed to apply plugin 'com.android.internal.application'.
   > The 'com.android.library' (or 'com.android.application') plugin is not
     compatible with the 'org.jetbrains.kotlin.multiplatform' plugin since AGP 9.0.
```

Per Android's own migration docs
(`developer.android.com/kotlin/multiplatform/plugin`), AGP 9.0 replaced
`com.android.library` + `org.jetbrains.kotlin.multiplatform` with a
dedicated `com.android.kotlin.multiplatform.library` plugin for KMP
library modules. Critically, the same docs state: *"There isn't a
direct replacement for configuring a Kotlin Multiplatform module using
[the] com.android.application plugin."* The recommended path for an
application that needs a KMP Android target (our `:composeApp`) is to
split it into a plain `com.android.application` module plus a separate
`com.android.kotlin.multiplatform.library` module holding the KMP/UI
code - a real structural change to ADR 0001's module boundaries.

The new plugin also has open, acknowledged issues as of AGP 9.2 (e.g.
Compose Multiplatform preview `NullPointerException`, only fixed in
Android Studio Otter 2 Feature Drop / AGP 9.0.0-alpha13 per the same
migration page) and drops build variants entirely, which we may need
later (e.g. a `debug`/`release` split is already implicit in every
module here).

AGP's own error output names the immediate, documented bypass: set
`android.builtInKotlin=false` and `android.newDsl=false`.

## Decision

Set `android.builtInKotlin=false` and `android.newDsl=false` in the
root `gradle.properties`, keeping all four modules
(`:shared`, `:domain`, `:data`, `:composeApp`) on the
`com.android.library` / `com.android.application` +
`org.jetbrains.kotlin.multiplatform` plugin combination that ADR 0001
and ADR 0002 already assume, rather than migrating to
`com.android.kotlin.multiplatform.library` now.

## Consequences

- The build emits deprecation warnings (`android { }` extension block
  deprecated under `newDsl=false`, KMP+`com.android.library`
  compatibility deprecated) on every configuration. These are noisy but
  non-fatal; AGP 10.0 is expected to remove the legacy path entirely.
- This ADR's decision has a shelf life. Before upgrading to AGP 10.0,
  the project must either migrate `:shared`, `:domain`, `:data` to
  `com.android.kotlin.multiplatform.library` and split `:composeApp`
  into an application module + a KMP library module, or find whatever
  successor compatibility path AGP 10 offers. Track this as a Phase-10
  (Performance/tooling hardening) or earlier follow-up, not indefinitely
  deferred debt.
- All four modules stay structurally identical (same plugin set, same
  `android { }` DSL), which was judged more valuable during Phase 0/1
  than being on the "recommended" plugin for library modules only,
  since a mixed plugin model across modules would be harder to reason
  about with no compiler feedback loop advantage gained (this session
  now has one, but future sessions restarting from memory should not
  need to hold two different Android Gradle plugin mental models).

## Alternatives Rejected

- **Migrate now to `com.android.kotlin.multiplatform.library` for
  `:shared`/`:domain`/`:data`, split `:composeApp`**: rejected for
  Phase 0/1 scope - it changes ADR 0001's module boundaries, has open
  Compose-preview bugs on the application side, and drops build-variant
  support the project may need. Revisit when AGP 10 forces the issue.
- **Pin back to AGP 8.x**: rejected - it would mean building against a
  known-superseded AGP release while JDK 25 / Android SDK 36 compat
  with AGP 8.x is itself unverified, trading one unknown for another.

## Follow-up Actions

- Re-evaluate this ADR before any AGP 10.0 upgrade.
- Track known-issue fix versions for
  `com.android.kotlin.multiplatform.library` mentioned above in case an
  earlier, opportunistic migration becomes attractive.
