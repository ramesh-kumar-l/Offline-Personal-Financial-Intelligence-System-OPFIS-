# OSS README Positioning Strategy

This document is the *strategy* behind the OSS-facing changes applied
directly to the root `README.md` (see that file for the result — this
avoids maintaining two copies of the same text that could drift apart).

## Positioning

**Tagline**: "Your financial life, remembered — entirely offline."

**One-sentence positioning**: OPFIS is an offline-first, AI-native
personal financial intelligence platform: every screen runs against a
single SQLCipher-encrypted local database, and its AI assistant cites
the exact accounts/transactions/documents behind every answer — no
server, no cloud AI call, no account required.

**What to lead with** (in order, matching what a first-time GitHub
visitor scans for):
1. What it is, in one line (the tagline above).
2. Proof it's real and finished: v1.0.0, all 12 phases closed, green
   build gate, a real packaged installer.
3. The differentiator: offline-only by architecture + citation-backed
   AI answers, not "cloud AI with an offline mode."
4. A link to `DEMO.md` for anyone who wants to see it working before
   reading further.

## Features (for the README's "What it does" section)

Already accurate in the current `README.md` — the 7-screen feature
list (Dashboard, Search, Vault, Memory, Assistant, Security, Data)
does not need rewriting, only better framing above it. See Section
"What it does" of the existing README for the verified feature text.

## Badges to add

Static, non-CI-dependent badges only (no external service badges that
would silently go stale or imply infrastructure that doesn't exist):
- License badge (MIT — matches the existing `LICENSE` file).
- A "Kotlin Multiplatform" / "Compose Multiplatform" badge pair, since
  that's a genuine, checkable technical fact about the repo.
- Explicitly **do not** add a build-status/CI badge unless a real CI
  workflow exists and is currently passing — an unverified or
  decorative badge undermines the credibility the rest of this
  strategy is built on.

## Quick Start

The actual "5-minute path" is extracted into a new, separate
`QuickStarterGuide.md` (see that file) rather than expanded inline in
`README.md` — keeps `README.md` scannable while giving OSS visitors
who just want to run it a single, minimal-friction file.

## Examples

`DEMO.md` already serves this role (a scripted, offline, ~10-minute
walkthrough of every screen). The README's positioning should link it
prominently rather than duplicate its content — see
`documents/08-demo-examples-pack.md` for the distribution-ready
adaptation of that walkthrough.

## Roadmap (for the README)

Link `documents/10-future-roadmap.md` rather than inlining a roadmap
in the README itself — keeps the README stable while the roadmap can
be updated independently as priorities shift.

## Contribution Model

No `CONTRIBUTING.md` exists yet and none is being added in this pass
— per `documents/00-leverage-strategy.md`'s Pareto cut, a contribution
guide is premature before any external contributor exists. The README
change adds only a one-line pointer stating the project is
single-maintainer today and issues/PRs are welcome, which is honest
rather than implying an established contribution process that doesn't
exist yet.

## What changed in README.md

Additive only, per the approved plan (no existing section removed or
restructured):
- A tagline line under the title.
- License + KMP/Compose badges near the top.
- A new "Documentation" section linking `QuickStarterGuide.md` and the
  `documents/` folder (all 10 artifacts), alongside the existing links
  to `VISION.md`/`PRD.md`/`ROADMAP.md`/`DEMO.md`/`CHANGELOG.md`.
- A one-line "Contributing" note under the existing "License" section.
