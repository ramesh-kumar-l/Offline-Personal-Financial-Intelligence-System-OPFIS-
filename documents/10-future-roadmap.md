# Future Roadmap

`ROADMAP.md` defines no Phase 13 — OPFIS v1.0.0 closes the roadmap as
originally scoped. Everything below is forward-looking, reorganized by
time horizon from the real items already tracked in
`project-memory-bank/26-active-initiatives.md`. Nothing here is new
invented scope; this is a reorganization for readability, not a new
commitment.

## Next 3 Months

- **Empirically measure the 3 performance-budget targets** (cold
  start <1s, search <100ms, dashboard <300ms) on a real device/desktop
  — the top-ranked open item; see `documents/05-benchmark-report.md`.
- **Provision code-signing** — a Desktop certificate and an Android
  release-signing keystore, so `packageDistributionForCurrentOS` and
  `bundleRelease` can produce artifacts a non-technical user can
  actually install. Owner action (credentials cannot be generated or
  committed by an automated session).
- **Create and push the `v1.0.0` git tag**, and decide whether to make
  the repository public — both deliberately left as explicit owner
  actions.
- **Expand `:composeApp` UI test coverage** beyond `LockScreenBody` to
  the other ~9 screens, using the same `XScreenBody`-is-injection-free
  pattern.

## Next 6 Months

- **Build-verify macOS DMG / Linux DEB packaging** on machines with
  those OSes (the current development environment is Windows-only, so
  only the MSI target format has been build-tested despite all three
  being configured).
- **Add Android instrumented tests** once a real emulator/device is
  available in the development environment — the encrypted-driver
  path, biometric auth, OCR, and file-picker actuals have never run on
  real Android hardware.
- **Full OS keychain/DPAPI integration for Desktop's database key** —
  closing ADR 0005's explicitly acknowledged weak point.
- **Publish the blog series** (`documents/07-blog-series.md`) and
  pursue conference talk submissions (`documents/09-talk-deck-
  outline.md`), timed after the credibility gaps above (signing,
  measured performance) are closed so the public-facing material has
  nothing to walk back.

## Next 12 Months

- **A real local LLM/embedding model binding for `LocalAiPort`**, if
  and when one becomes feasible in this environment — upgrading from
  `RuleBasedLocalAiEngine` without changing the port contract the rest
  of the app depends on.
- **A presentation layer for the `Relationship`/`KnowledgeGraph`
  engine** — domain and data layers are already complete and tested;
  only the UI was deferred.
- **Reassess ADR 0004** before any AGP 10.0 upgrade, per that ADR's
  own explicit shelf-life clause.
- **Decide the startup-vs-pure-OSS direction based on real signal**
  (see `documents/00-leverage-strategy.md` §7-8) rather than the
  hypotheses this document and the strategy doc currently rely on —
  by month 12, actual usage/interest data (if any exists) should
  replace those hypotheses.
