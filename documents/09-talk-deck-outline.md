# Talk / Presentation Deck Outline

Text-only outline (no slide file produced — this environment has no
slide-authoring tool; use this as the script for building one in
Keynote/Slides/reveal.js).

## Talk Title

**"Financial memory without the cloud: building an explainable AI
assistant that never makes a network call."**

## Target Audience

Kotlin/Android/Compose Multiplatform conferences (KotlinConf-style),
or a local-first/privacy-engineering track at a general software
conference. Assumes a technical audience familiar with mobile/desktop
app development; does not assume familiarity with either finance
domain modeling or AI/ML.

## Outline

1. **The hook** (2 min) — live or recorded demo: ask the Assistant a
   question with the network disabled, get an instant, cited answer.
2. **The constraint that shaped everything** (3 min) — "offline-only"
   isn't a mode, it's the only mode; what that ruled out immediately
   (cloud search, cloud LLM, hosted sync).
3. **Architecture under that constraint** (7 min) — the 4-module Clean
   Architecture, why the module graph itself enforces the boundary,
   Kotlin Multiplatform across Android + Desktop from one codebase.
4. **The hardest tradeoff: encryption key storage on two platforms**
   (5 min) — Android's Keystore-backed key vs. Desktop's honestly-
   weaker file-based key (ADR 0005); why shipping the documented
   asymmetry beat blocking the release on platform parity.
5. **Explainable AI without an LLM** (6 min) — how a deterministic
   rule engine can satisfy "AI-native" by citing its sources, and why
   that's a *stronger* trust property than a bigger model with no
   citations.
6. **A real production hazard, live** (4 min) — the AGP 9
   compatibility break (ADR 0004): the actual error, the fix, and the
   decision to time-box it rather than do a larger migration under
   pressure.
7. **What's still not done, on purpose** (3 min) — the honestly-
   tracked gaps (no signing yet, performance targets unmeasured,
   thin UI test coverage) — modeling how to talk about incompleteness
   without undermining credibility.
8. **Close / call to action** (2 min) — link to the repo, `DEMO.md`,
   and the memory-bank pattern as a reusable technique independent of
   the finance domain.

**Total**: ~32 minutes, fits a standard 30-35 minute conference slot
with Q&A.

## Key Slides

- Title slide with the tagline from `documents/04-oss-readme-
  positioning.md`.
- The architecture diagram from `documents/02-architecture-
  document.md` (module graph + dependency direction).
- A side-by-side of Android vs. Desktop key-management strategy (ADR
  0005), visually marking the asymmetry rather than hiding it.
- The actual AGP 9 error message, verbatim, as a slide on its own —
  authenticity over a polished paraphrase.
- The release-checklist format (checked/unchecked items with reasons)
  as the closing "how we talk about done" slide.
