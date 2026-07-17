# Roadmap
Refer to ROADMAP.md. Update after each completed phase.

Status: Phases 0-11 closed. Phase 9 (Import/Export) and Phase 10
(Performance) were implemented 2026-07-16 but left build-unverified
(no JDK/Android SDK in that session's environment). Phase 11 (Testing:
unit, integration, UI, security tests, performance benchmarks)
implemented 2026-07-17, now that a real JDK 21 + Android SDK 36
toolchain is available - see `19-testing-strategy.md`. This phase's
first order of business was running the long-overdue build gate
against Phases 9 and 10: it found and fixed several real bugs
(detekt violations, a missing `FakeRelationshipRepository.observeAll()`
override, a `List<Pair<String,String>>` vs `List<Pair<String,String?>>`
type-inference failure, and a test asserting a false invariant about
`SqlDriver` behavior after close - see `05-current-state.md` for the
full list). `./gradlew ktlintCheck detekt allTests assemble` is now
**green** for both Android and Desktop, covering Phases 0-11.
Remaining, explicitly-scoped-out this phase: automated performance
benchmarks (targets are structurally justified, not measured - see
`20-performance-budget.md`) and UI test coverage beyond one screen
(`LockScreenBody`) - see `19-testing-strategy.md`'s "Known gaps".
Per ROADMAP.md's "stop for review" policy, this session stops before
Phase 12 (MVP Release) - see `26-active-initiatives.md`.