# Active Initiatives

## Current phase

Phase 4 - Search (ROADMAP.md) implemented and tested; Phases 0-3 remain
closed. Phase 5 (Document Intelligence: PDF import, image import, OCR,
document indexing, receipt vault) now in progress per explicit owner
instruction.

## Active tasks

- Implement Phase 5 (Document Intelligence) - see `05-current-state.md`
  for live status.
- When an Android emulator/device becomes available, add an
  instrumented test for the real `AndroidSqliteDriver` + `SupportFactory`
  encrypted path (carried over from Phase 1 - still open).
- When `node`/npm becomes available in this environment, run the
  `dataviz` skill's `scripts/validate_palette.js` against the dashboard
  chart palette to formally confirm CVD-safety (carried over from
  Phase 3 - not blocking, current mitigation is icon+text+color).

## Blockers

None currently.
