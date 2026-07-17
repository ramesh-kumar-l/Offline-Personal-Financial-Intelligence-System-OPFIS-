# Engineering Thesis

## Title

**Financial memory, not financial bookkeeping: why offline-first, local
AI, and explainable answers belong together.**

## Vision

Personal finance software has spent two decades getting better at
*recording* — categorizing transactions faster, syncing more banks,
rendering prettier charts. It has not gotten better at *remembering*.
A user who wants to know "why did I take out that loan in 2023" or
"what was my net worth before I bought this asset" is on their own,
digging through PDFs and old emails, because the software they used to
track the transaction never treated it as part of a permanent,
explainable financial history.

OPFIS's vision (VISION.md) is to build that permanent, explainable
financial memory — entirely on the user's own device, answerable in
natural language, with every answer traceable to the specific
account, transaction, or document behind it.

## Problem

Three separate problems compound in the current personal-finance
software category:

1. **Fragmentation.** A person's financial life is spread across
   banks, brokers, insurers, tax filings, and paper/PDF documents,
   with no single system that treats all of it as one coherent
   record.
2. **No memory.** Existing apps optimize for the current month's
   budget, not for "what happened and why" across years. Once a
   transaction scrolls off-screen, the *reasoning* behind it (a note,
   a linked document, a related event) is gone.
3. **Trust.** Cloud-based finance apps require handing raw financial
   data to a third party, whose incentives (engagement, monetization,
   acquisition) are not the user's incentives. AI features built on
   top of that data compound the exposure — a cloud LLM call means the
   data leaves the device by definition.

## Why Now

Two trends make this solvable today in a way it wasn't five years ago:

- **On-device capability.** Full-text search (SQLite FTS5), local OCR
  (Tesseract, ML Kit), and encrypted embedded databases (SQLCipher)
  are mature enough to run a real financial-intelligence workload
  entirely offline, on commodity hardware, without a server.
- **Rising demand for local-first, privacy-respecting software.**
  This is treated in the project's own planning as a strategic bet
  (VISION.md §16), not a proven market fact — but the direction of
  regulation and user sentiment toward data minimization is consistent
  with it.

OPFIS deliberately does not wait for a bundled neural LLM to become
practical in this environment; its `Assistant` screen ships today with
a deterministic, rule-based engine that already satisfies the harder
requirement — every answer is explainable and cited — while remaining
upgradeable to a real local model later without changing the contract
the rest of the app depends on (`LocalAiPort`, see
`project-memory-bank/15-ai-runtime.md`).

## Differentiation

- **Offline by architecture, not by fallback.** There is no server to
  degrade from. The whole application, including its AI assistant, is
  built and tested to run with the network disabled — proven directly
  in `DEMO.md`'s walkthrough, not just claimed.
- **Explainable by construction.** The assistant's answers are not a
  free-text generation with a disclaimer; each one is composed from
  and cites concrete rows in the user's own encrypted database.
- **A platform, not a single app.** OPFIS is positioned as the first
  application on a reusable substrate (the "Memory Intelligence
  Platform" — VISION.md §14: local semantic memory, knowledge graph,
  search, encryption, AI runtime, document intelligence), meaning the
  architecture was deliberately built to outlive this one product.

## Long-term Impact

If the thesis holds, OPFIS demonstrates that a category of software
usually associated with cloud dependency — AI-assisted personal
finance — can be built with zero server dependency and stronger user
trust as a direct consequence, not a marketing claim. Independent of
whether OPFIS itself finds users, the reusable pieces (the module
template, the ADR discipline, the memory-bank AI-pairing convention —
see `documents/00-leverage-strategy.md` §5) are a durable output of
this engineering effort on their own.
