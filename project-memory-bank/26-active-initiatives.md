# Active Initiatives

## Current phase

Phase 8 - Security (ROADMAP.md) implemented and tested; Phases 0-7
remain closed. Phase 9 (Import/Export: CSV, JSON, encrypted backup,
restore) is next - not yet started, no owner instruction received to
begin it.

## Active tasks

- Await owner review/direction on Phase 9 (Import/Export) before
  starting - see `04-roadmap.md`.
- When Phase 9 builds the real backup/restore UI, wire
  `RecordAuditEventUseCase` around it directly (`AuditEventType`
  already has `BACKUP_EXPORTED`/`BACKUP_RESTORED` ready) - Phase 8
  deliberately did not add `ExportBackupUseCase`/`RestoreBackupUseCase`
  since there was no caller for them yet.
- Full OS keychain/DPAPI integration for Desktop's database key is
  still open - Phase 8 only added owner-only file permissions/ACL
  hardening, not the ADR 0005-flagged full integration - see
  `09-security-model.md`.
- Consider a real local LLM/embedding model binding for `LocalAiPort`
  if/when one becomes feasible in this environment (bundled weights,
  ONNX Runtime/llama.cpp integration) - `RuleBasedLocalAiEngine` is a
  deliberate, user-confirmed placeholder, not the end state - see
  `15-ai-runtime.md`.
- Build a presentation layer for Phase 6's `Relationship`/
  `KnowledgeGraph` engine (domain + data + tests are complete; only the
  UI was deferred - see `13-memory-engine.md`).
- Consider whether/which existing use cases should auto-generate
  `MemoryEvent`s (e.g. "goal reached", "budget exceeded") - deliberately
  out of scope for Phase 6's first pass; revisit once there's a clearer
  picture of which events are actually worth surfacing.
- When an Android emulator/device becomes available, add an
  instrumented test for the real `AndroidSqliteDriver` + `SupportFactory`
  encrypted path (carried over from Phase 1 - still open) and verify
  Phase 5's OCR (ML Kit) + `DocumentPicker` Android paths.
- When `node`/npm becomes available in this environment, run the
  `dataviz` skill's `scripts/validate_palette.js` against the dashboard
  chart palette to formally confirm CVD-safety (carried over from
  Phase 3 - not blocking, current mitigation is icon+text+color).

## Blockers

None currently.
