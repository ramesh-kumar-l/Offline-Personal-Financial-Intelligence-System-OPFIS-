# Roadmap
Refer to ROADMAP.md. Update after each completed phase.

Status: **Phases 0-12 closed - v1.0.0 MVP released 2026-07-18.**
Phase 12 (MVP Release: documentation, demo, release notes, packaging,
version 1.0) added no production code - it rewrote `README.md` for
the actual v1.0.0 feature set (previously still describing the Phase
0 scaffold), added `CHANGELOG.md` and `DEMO.md`, filled in
`25-release-checklist.md`, configured Desktop native-distribution
packaging (`compose.desktop.application.nativeDistributions`: MSI/
DMG/DEB target formats via `jpackage`), and bumped both the Android
`versionName` and Desktop `packageVersion` to `1.0.0`. Both
`./gradlew :composeApp:createDistributable` and
`./gradlew :composeApp:packageDistributionForCurrentOS` were verified
against the real toolchain, producing a working unsigned
`OPFIS-1.0.0.msi` installer. `./gradlew ktlintCheck detekt allTests
assemble` remains green for both Android and Desktop after these
changes (`BUILD SUCCESSFUL in 11s`, 400 tasks). See
`25-release-checklist.md` for the full sign-off against ROADMAP.md's
Definition of Done, including the honestly-tracked open items (no
code-signing keys/keystore exist in this environment, so the MSI is
unsigned and no signed Android artifact was produced - this is a
deliberate stop, not an oversight; see that file's "Security
reviewed"/"Packaging" sections). Per ROADMAP.md's "stop for review"
policy, this closes the roadmap as scoped - no Phase 13 is defined;
future work is tracked in `26-active-initiatives.md`.