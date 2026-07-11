# Active Initiatives

## Current phase

Phase 0 - Foundation (ROADMAP.md). Scaffold implemented; blocked on
build verification (see `05-current-state.md` "Known gaps").

## Active tasks

- Owner (human) to run the first real `./gradlew` (after generating
  the wrapper jar) or open the project in Android Studio, on a machine
  with JDK 17+ and an updated Android SDK (`platform;android-35`,
  current `build-tools`, `cmdline-tools`), and report any compile
  errors back for fixing.
- Once build passes: Phase 0 can be formally closed and Phase 1 (Core
  Persistence - SQLCipher, schema, repository layer, migrations) can
  begin, pending explicit approval per the phase-execution policy.

## Blockers

- Local dev machine (this session's environment) lacks a JDK, has an
  outdated Android SDK (`D:\AndroidSDK`, max API 29, no cmdline-tools),
  and the sandboxed shell has no outbound internet access - none of
  these could be fixed from within this session. Not a code blocker;
  an environment one.
