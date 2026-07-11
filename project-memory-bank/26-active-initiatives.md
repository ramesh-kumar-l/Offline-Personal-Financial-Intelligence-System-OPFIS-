# Active Initiatives

## Current phase

Phase 3 - Dashboard & UX (ROADMAP.md) implemented and tested; Phases
0-2 remain closed. Awaiting owner review/approval before Phase 4
(Search: FTS5, global search, filters, timeline search, tags) begins -
per the phase-execution policy in SYSTEM.md, do not start Phase 4
autonomously.

## Active tasks

- Owner to review the Phase 3 implementation (dashboard UI, net worth/
  cash flow calculators, chart design, minimal search entry point
  documented in `02-system-architecture.md`/`14-search-engine.md`) and
  either approve moving to Phase 4 or request changes.
- When an Android emulator/device becomes available, add an
  instrumented test for the real `AndroidSqliteDriver` + `SupportFactory`
  encrypted path (carried over from Phase 1 - still open).
- When `node`/npm becomes available in this environment, run the
  `dataviz` skill's `scripts/validate_palette.js` against the dashboard
  chart palette to formally confirm CVD-safety (carried over from
  Phase 3 - not blocking, current mitigation is icon+text+color).

## Blockers

None currently.
