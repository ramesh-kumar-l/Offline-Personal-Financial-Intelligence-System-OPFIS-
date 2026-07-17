# OPFIS Leverage Strategy

Analysis of the OPFIS project (v1.0.0, shipped 2026-07-18, all 12
ROADMAP.md phases closed) for career, open-source, and startup
leverage. Objective weighting confirmed with the project owner: mixed
(career/OSS/startup roughly balanced), career altitude: Principal.

All facts below are grounded in the repository as it exists today
(`VISION.md`, `PRD.md`, `docs/adr/`, `project-memory-bank/`,
`README.md`, `CHANGELOG.md`). Where a claim would require data that
doesn't exist yet (benchmarks, user adoption, revenue), it is marked
explicitly as a hypothesis or future measurement, never asserted as
fact.

## 1. Project Analysis

**Core innovation.** OPFIS reframes "personal finance app" as
"financial memory system": instead of transaction logging, it builds
an explainable, queryable, permanent record of a person's financial
life — assets, liabilities, documents, notes, relationships between
entities — entirely offline, with a local rule-based AI that cites its
sources rather than an opaque cloud model.

**Technical complexity.** Real: 4-module Clean Architecture enforced
by the Gradle build graph (not convention), Kotlin Multiplatform
across Android + Desktop from a single codebase, SQLCipher-encrypted
SQLite with platform-specific driver/key strategies (documented in ADR
0005), local OCR on both platforms, SQLite FTS5 full-text search, a
deterministic explainable-AI engine, biometric/audit-log security, and
full offline import/export/backup. Five ratified ADRs document the
non-obvious tradeoffs behind these choices.

**Differentiators.** (1) Offline-only by architecture, not as a
degraded fallback mode — there is no server and no cloud code path to
disable. (2) Every AI answer cites its source data (`Assistant`
screen) instead of returning an unverifiable free-text response. (3)
The project is built as the first application on a stated reusable
substrate ("Memory Intelligence Platform" — see VISION.md §14),
meaning the architecture is deliberately designed for a second
application to reuse it, not just to finish this one.

**Market relevance.** Privacy regulation trends and "cloud fatigue"
make local-first software a live category (note: this is a *strategic
bet* stated in VISION.md §16, not a market-research finding — treat it
as a thesis to test, not a validated trend).

**Career signal value.** High. The project demonstrates: architecture
decisions made and documented *before* code (ADRs precede
implementation, not retrofitted), a working multi-platform build with
a real packaged installer, explicit tracking of what is *not* done
(release checklist, active-initiatives list) rather than a polished
facade — this last trait is unusually rare and reads as senior
engineering judgment to anyone who reviews the repo closely.

**Startup potential.** Real but unproven — no user has used it outside
this development environment. The honest state is "a working,
architecturally sound MVP with zero market validation," which is a
normal and fine place for a side project to be. Treat startup framing
as optionality, not a current claim of traction.

**Open-source potential.** Strong structural fit: MIT-licensed
already, a genuinely novel positioning (explainable local financial
AI) that's easy to explain in one sentence, and a codebase disciplined
enough (ADRs, memory bank, test coverage on the domain/data layers) to
survive outside contributors reading it cold.

## 2. Artifact Universe

Full inventory considered, before Pareto scoring, grouped by category:

- **Architecture**: system architecture document, ADR collection,
  module-boundary diagram, data-flow diagram, scaling/reliability memo
- **Engineering**: engineering thesis, benchmark report, testing
  strategy writeup, security model writeup, coding-standards guide
- **Product**: PRD refresh, competitive positioning one-pager, feature
  roadmap, persona/use-case pack
- **Research**: "why local AI now" essay, "explainable AI without an
  LLM" technical writeup, offline-first design patterns catalog
- **Career**: Principal Engineer case study, resume bullets, LinkedIn
  post series, interview talking-points doc
- **Content**: blog series, demo walkthrough, talk deck, README
  positioning rewrite
- **Open Source**: contribution guide, issue templates, OSS README,
  code-of-conduct, quick-start guide
- **Enterprise**: (considered, rejected — no enterprise buyer exists
  for a single-user offline app in its current form; out of scope)
- **Community**: Discord/discussions setup, "good first issue"
  labeling pass (considered, deferred — needs actual contributor
  interest first, not worth producing before any user exists)
- **Learning**: reusable-pattern catalog (memory-bank convention, ADR
  template, Fake-repository test pattern, Screen/ScreenBody split)

## 3. Pareto Ranking

Scored 1-10 per dimension; Leverage Score = weighted mean favoring
Career/OSS/Distribution per the confirmed "mixed" objective, penalized
by Effort.

| Artifact | Career | OSS | Startup | Learning | Distribution | Effort | Leverage |
|---|---|---|---|---|---|---|---|
| Principal Engineer case study | 10 | 4 | 3 | 6 | 5 | 4 | **9** |
| Architecture document | 8 | 7 | 4 | 8 | 6 | 4 | **8** |
| ADR collection | 8 | 6 | 3 | 9 | 5 | 3 | **8** |
| Engineering thesis | 7 | 7 | 6 | 6 | 6 | 3 | **8** |
| OSS README positioning | 5 | 9 | 5 | 3 | 9 | 2 | **8** |
| QuickStarterGuide | 3 | 8 | 3 | 3 | 8 | 2 | **7** |
| Future roadmap | 4 | 6 | 6 | 4 | 5 | 2 | **6** |
| Blog series (outline) | 6 | 7 | 4 | 6 | 7 | 3 | **7** |
| Demo/examples pack | 4 | 7 | 5 | 4 | 6 | 3 | **6** |
| Talk deck outline | 7 | 5 | 3 | 4 | 5 | 3 | **6** |
| Benchmark report | 5 | 5 | 3 | 5 | 4 | 4 | **5** |
| Contribution guide | 2 | 6 | 2 | 2 | 4 | 2 | 4 |
| Enterprise one-pager | 2 | 1 | 5 | 1 | 2 | 4 | 2 |
| Persona/use-case pack | 2 | 3 | 5 | 2 | 3 | 3 | 3 |

Bottom four (contribution guide, enterprise one-pager, persona pack,
community setup) are cut from this pass — real but premature before
any external contributor or user exists. They're listed as Month-2/3
items in the 30-day plan's follow-on, not produced now.

## 4. The 80/20 Artifact Set

The 10 artifacts selected (at or above Leverage Score 5), each
produced as its own file under `documents/`:

| # | Artifact | Purpose | Audience | Priority |
|---|---|---|---|---|
| 1 | Engineering Thesis | State the "why" in one page, reusable in any pitch | Recruiters, readers of the repo, future collaborators | P0 |
| 2 | Architecture Document | Prove the system is real engineering, not a demo | Interviewers, technical reviewers, contributors | P0 |
| 3 | ADR Collection | Show decision discipline over time | Interviewers, contributors evaluating the codebase | P0 |
| 4 | OSS README Positioning | Convert repo visitors into readers/stars/contributors | GitHub visitors | P0 |
| 5 | Benchmark Report | Honest performance status — credibility, not hype | Technical reviewers | P1 |
| 6 | Principal Engineer Case Study | The single highest-leverage career artifact | Hiring panels, promo committees | P0 |
| 7 | Blog Series (outline) | Compounding distribution over months | Broader dev audience | P1 |
| 8 | Demo & Examples Pack | Make the value concrete in under 2 minutes of reading | GitHub visitors, first-time users | P1 |
| 9 | Talk Deck Outline | Optionality for conference/meetup submission | Conference committees | P2 |
| 10 | Future Roadmap | Signals ongoing momentum, not a finished/abandoned repo | GitHub visitors, contributors | P1 |

Effort for the full set: roughly 2-3 focused sessions of writing (this
session produces all 10 outlines plus the README/QuickStartGuide
edits). Expected ROI is front-loaded on items 1, 2, 3, 4, 6 — those
five alone cover the Principal-level interview narrative *and* the
GitHub-visitor conversion path.

## 5. Reusable Asset Catalog

Patterns worth extracting and reusing beyond OPFIS itself:

- **4-module Clean Architecture template** (`:shared -> :domain ->
  :data -> :composeApp`, ADR 0001) — a minimal, YAGNI-respecting
  starting module graph for any KMP app that needs a real enforced
  architecture boundary from day one.
- **ADR template** (`docs/adr/000X-*.md`: Context, Problem, Options
  Considered, Decision, Consequences, Alternatives Rejected,
  Follow-up Actions) — already in use, directly reusable in any
  project; the "Alternatives Rejected" section is the part most teams
  skip and is exactly what makes these ADRs valuable as career
  artifacts.
- **`project-memory-bank/` convention** — a numbered set of topic
  files (vision, architecture, tech stack, security model, testing
  strategy, current state, active initiatives, session handoff) that
  gives an AI pairing partner (or a new human contributor) a
  compressed "save state" without re-reading the whole codebase. This
  is itself a reusable AI-collaboration pattern, independent of
  OPFIS's domain — worth its own blog post (see item 7, article 6).
- **`Fake*Repository` test pattern** — hand-written fakes per
  repository interface instead of a mocking framework, used
  throughout `:domain` tests.
- **`XScreen` / `XScreenBody` split** — `XScreen` does Koin injection
  and state hookup, `XScreenBody` is pure, injection-free composable
  layout — makes UI testable without a DI container (proven by the
  one existing UI test, `LockScreenBody`).
- **Release checklist template** (`25-release-checklist.md`) — a
  Definition-of-Done checklist that explicitly lists unchecked items
  with a one-line reason each, rather than only showing what's done.

## 6. Career Leverage Analysis

**FAANG / Staff / Principal interviews.** The case study (artifact 6)
is written to answer the standard "tell me about a hard technical
decision" and "tell me about a system you designed end-to-end"
prompts directly, using the 5 real ADRs as the evidentiary spine
rather than a from-memory anecdote. ADR 0004 (AGP9 compatibility
break) is a strong "debugging under real production constraints"
story: a build failure, correct root-causing via the vendor's own
migration docs, and an explicit "this decision has a shelf life"
follow-up — that's a materially better answer than a hypothetical.

**Resume / LinkedIn.** Bullet-ready framing, e.g.: "Designed and
shipped a Kotlin Multiplatform (Android + Desktop) financial
intelligence application from a blank repo to a packaged v1.0.0
release across 12 phases, including an encrypted-storage architecture
spanning two platform-specific key-management strategies and a
citation-backed local AI assistant with zero cloud dependency."

**Conference talks.** The differentiator ("explainable AI without an
LLM, entirely offline") is talk-worthy on its own; see artifact 9 for
a full outline.

**Technical leadership signal.** The memory-bank + ADR discipline
demonstrates the specific Principal-level behavior of writing
decisions down *before* they're needed by someone else — this is the
single most transferable signal in the whole repository, more than
any one line of code.

## 7. Startup Leverage Analysis (hypotheses, not claims)

**Potential customers**: privacy-conscious individuals, FIRE-community
members already using spreadsheets, freelancers without a company
finance stack (all directly named in VISION.md §10 — no invented
segments).

**PMF signals to watch for** (none exist yet — these are what to
measure if OPFIS is ever distributed beyond this environment): repeat
weekly opens without a reminder mechanism, users importing their real
financial documents rather than test data, unprompted feature
requests for a specific missing entity type (e.g. cryptocurrency
accounts).

**Monetization paths** (all currently hypothetical, all consistent
with VISION.md §9's non-goals — no ads, no data sale): a one-time paid
desktop license, an optional paid companion service (e.g. multi-device
sync via user-controlled infrastructure, not OPFIS-hosted), or staying
fully free/OSS with a sponsored-development model.

**Distribution channels**: GitHub (organic, via the README rewrite),
Hacker News "Show HN" for a genuinely novel local-AI angle, FIRE/
personal-finance communities (Reddit r/personalfinance-adjacent,
privacy-focused forums) — not paid channels, consistent with a
bootstrapped OSS-first approach.

**Moat**: weak today by design — the actual moat, if this becomes a
product, is the accumulated financial-memory dataset a long-term user
builds locally (switching cost via their own data's depth, not
vendor lock-in) plus the trust earned by genuinely never having a
server to breach.

**Risks**: no signing/distribution pipeline yet (blocks any
non-technical user from installing it), no real device testing (a
correctness risk before wider distribution), single-maintainer
bus-factor, and the core "local AI" claim currently means a
deterministic rule engine, not a neural model — this must stay
honestly described in any public-facing material (see artifact 4) to
avoid an credibility gap on first contact with a technical audience.

## 8. 30-Day Execution Plan (3 hrs/day)

**Week 1 — Ship the leverage artifacts (this session's output).**
Finalize and proofread all 10 `documents/` artifacts, apply the
README/QuickStarterGuide changes, verify no fabricated claims slipped
in (cross-check against `25-release-checklist.md`'s honest gap list).

**Week 2 — Close the credibility gaps the artifacts expose.**
Empirically measure the 3 performance-budget targets on a real
desktop (top item in `26-active-initiatives.md`); if time remains,
provision a self-signed or low-cost code-signing setup so
`documents/04` and `documents/08` can point at an installable
artifact, not just source.

**Week 3 — Distribution.** Publish the repository publicly (owner
action — tagging/pushing was deliberately not done automatically this
session), post article 1 of the blog series (artifact 7), submit a
"Show HN" or equivalent if the owner chooses to pursue OSS
distribution.

**Week 4 — Feedback loop.** Read any real reactions (issues, comments,
interview conversations that reference the case study), update
`documents/10-future-roadmap.md`'s 3-month section based on what
actually generated interest, and decide whether to invest further in
the startup or pure-OSS/career direction based on real signal rather
than the hypotheses in section 7 above.
