# Demo & Examples Pack

Distribution-ready adaptation of `DEMO.md` for README/social/talk use.
The full step-by-step script lives in `DEMO.md` — this file exists to
package it for people who need the short version, plus illustrative
personas. No content here overrides `DEMO.md`; if the two ever
disagree, `DEMO.md` (the actually-tested script) is authoritative.

## Top Example Scenario

The single scenario worth leading with (condensed from `DEMO.md`):

1. Launch OPFIS with the network disabled.
2. Import a receipt or statement into **Vault** — text is extracted
   locally (OCR), no upload, immediately searchable.
3. Ask the **Assistant** a plain-English question ("what is my net
   worth") — the answer arrives instantly and cites the exact
   account/transaction it came from.
4. Export the full dataset as encrypted backup, then restore it — the
   app closes and reopens with the data intact, proving the round
   trip.

This four-step version proves the three claims that matter most:
offline operation, explainable AI, and real data ownership (a
working, verifiable export/restore, not a promise).

## Full Walkthrough

See `DEMO.md` at the repo root — the complete 7-section, ~10-minute
scripted walkthrough covering all seven screens (Dashboard, Vault,
Memory, Search/Timeline, Assistant, Security, Data), run entirely
offline against a fresh local database with no seed script required.

## Illustrative Personas

Explicitly **illustrative**, drawn directly from VISION.md §10's named
target-user categories — not real customers, not collected user
research. Framed as "who this is built for," not "who uses this
today":

**"The FIRE spreadsheet refugee."** Tracks net worth and goals in a
personal spreadsheet today because no app they trust holds years of
financial history without a subscription or a cloud account. Would use
OPFIS's Dashboard net-worth tracking and Goal-linked entities as a
direct spreadsheet replacement, with the Vault providing a reason to
finally stop keeping loose PDF statements in a downloads folder.

**"The privacy-conscious engineer."** Comfortable with technical
setup, uncomfortable handing financial data to a third party purely to
get AI-assisted insights. Would use the Assistant screen specifically
because every answer is source-cited and verifiable against their own
database, not a black-box response they have to trust blindly.

**"The freelancer without a finance stack."** No employer-provided
tools, income and expenses split across several accounts and
platforms. Would use Search/Timeline and the Memory screen's
milestone notes to reconstruct "what happened financially this
quarter and why" without a bookkeeper.

## What This Pack Deliberately Does Not Include

- Screenshots or video (no GUI capture capability in the current
  development environment — noted plainly rather than faked).
- Real customer quotes or usage statistics (none exist yet — see
  `documents/00-leverage-strategy.md` §7 for how to treat this
  honestly).
