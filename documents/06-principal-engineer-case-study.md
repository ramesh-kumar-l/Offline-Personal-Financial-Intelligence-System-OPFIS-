# Principal Engineer Case Study: Building OPFIS

Pitched at Principal altitude: the emphasis is on architecture-defining
tradeoffs, multi-year platform thinking, and judgment under real
constraints — not feature delivery velocity.

## Problem

Design and ship a personal financial intelligence application that
must satisfy a hard, non-negotiable constraint most competing products
in the category don't attempt: complete offline operation, including
its AI features, with the user's financial data never leaving their
device — while still delivering a genuinely intelligent (not just a
static-report) experience, across two platforms (Android and Desktop)
from one codebase, as the first application on top of a reusable
platform substrate intended to outlive this one product.

## Constraints

- **No cloud fallback, ever** — not "offline mode," but the only mode.
  This eliminates the easiest solutions to search (hosted search
  service), AI (cloud LLM API), and sync (hosted backend) in one
  stroke.
- **Two platforms, one codebase** — Android and Desktop/JVM have
  materially different capabilities for the two hardest technical
  problems (encryption key storage, biometric auth), which cannot be
  papered over with a shared abstraction that pretends they're the
  same.
- **Solo-built, in a development environment with no internet access
  for most of the build** — dependency versions had to be verified and
  the AGP-9 compatibility break (ADR 0004) diagnosed and resolved
  without the usual trial-and-error against a live package registry
  for long stretches.
- **No local LLM runtime available** — the "AI-native" requirement had
  to be met without the component most people would assume was
  required for it.

## Architecture

Four-module Clean Architecture (`:shared -> :domain -> :data ->
:composeApp`) with dependency direction enforced by the Gradle build
graph, not code review (ADR 0001) — see
`documents/02-architecture-document.md` for the full diagram and
component breakdown. The key structural decision: every
platform-divergent capability (encryption key storage, biometrics, OCR,
file I/O) is defined as an `expect` port in shared code and
implemented as a real `actual` per platform, rather than either
duplicating business logic per platform or forcing a lowest-common-
denominator abstraction that hides real platform differences.

## Tradeoffs

The five ratified ADRs are the actual tradeoff record for this
project, not a reconstruction:

- Module boundary granularity vs. YAGNI (ADR 0001).
- Multiplatform reach vs. build/runtime complexity (ADR 0002).
- DI simplicity vs. platform reach (ADR 0003).
- Toolchain currency vs. structural churn, under a real build-breaking
  vendor change mid-project (ADR 0004).
- Encryption uniformity vs. actually-available platform primitives —
  explicitly accepting and documenting that Desktop's key management is
  weaker than Android's, rather than hiding the asymmetry (ADR 0005).

The single highest-signal tradeoff for a Principal-level narrative is
ADR 0004: a real, uncontrolled, mid-project vendor breaking change
(AGP 9 dropping compatibility with the KMP + `com.android.application`
plugin combination), root-caused correctly against the vendor's own
migration documentation, resolved with the documented compatibility
flags rather than a larger structural migration under time pressure,
and explicitly time-boxed with a named trigger for revisiting it
("before any AGP 10.0 upgrade") — the kind of decision-with-an-
expiration-date that's easy to describe in principle and rare to see
actually written down.

## Impact

Stated as build/delivery outcomes — no user-adoption or business
metrics exist yet, and none are claimed:

- All 12 phases in `ROADMAP.md` implemented and closed, ending in a
  v1.0.0 MVP release.
- `./gradlew ktlintCheck detekt allTests assemble` green for both
  Android and Desktop targets at release.
- A real, working (unsigned) Windows installer (`OPFIS-1.0.0.msi`)
  produced and verified against the actual Compose Multiplatform
  packaging toolchain, discovering along the way that the toolchain
  bundles its own WiX Toolset — verified empirically by reading the
  build log, not assumed from documentation.
- Full domain and data-layer test coverage (every use case, every
  repository), plus a documented, honestly-tracked list of what
  remains untested (Android instrumented tests, most UI screens) —
  see `documents/05-benchmark-report.md` and
  `project-memory-bank/25-release-checklist.md` for the complete,
  unembellished picture.

## Lessons Learned

- **Writing the ADR's "Alternatives Rejected" section is the highest-
  leverage five minutes in the whole decision** — it's the part that
  turns a decision into a teachable artifact, and it's exactly the
  part most engineers skip under time pressure.
- **A platform asymmetry that's documented is a design decision; the
  same asymmetry undocumented is a landmine.** ADR 0005's explicit
  "acknowledged weak point" framing for Desktop's encryption key is
  the difference between an honest tradeoff and a latent security
  incident waiting to be discovered by someone else later.
- **Time-boxing a decision (ADR 0004's "shelf life") is more valuable
  than getting the decision permanently right.** Under real vendor
  churn, the correct move was often the smallest change that unblocks
  the build now, paired with a concrete, named trigger for revisiting
  it — not a larger "future-proof" rewrite attempted under pressure.
- **A memory bank isn't overhead, it's the artifact that makes
  solo, session-boundary-crossing engineering work auditable** — the
  same `project-memory-bank/` discipline that let this project resume
  cleanly across sessions is what made this case study possible to
  write accurately, from the actual decision record, rather than from
  memory after the fact.
