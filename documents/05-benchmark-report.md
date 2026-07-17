# Benchmark Report

**Status as of 2026-07-18 (v1.0.0): targets defined and structurally
justified; empirical measurement not yet performed.** This report
exists to state that honestly, not to present invented numbers. It
follows the same standard already applied in
`project-memory-bank/25-release-checklist.md`'s "Performance
validated" section.

## Metrics

Three named targets from `project-memory-bank/20-performance-budget.md`:

| Metric | Target |
|---|---|
| Cold start | < 1s |
| Search latency | < 100ms |
| Dashboard render | < 300ms |

## Methodology (planned, not yet executed)

No automated benchmark harness exists in the current development
environment (no `androidx.benchmark` module, no JMH setup). The
planned measurement procedure, once a real device/desktop is
available for manual timing:

1. **Cold start**: stopwatch/OS-level timing from process launch to
   the Dashboard screen's first composed frame, on a cold JVM/Android
   process (not a warm re-launch), averaged over multiple runs.
2. **Search latency**: timestamp the FTS5 query dispatch and its
   result-`Flow` emission for a representative query against a
   populated (not empty) database, averaged over multiple runs at a
   couple of realistic dataset sizes.
3. **Dashboard render**: timestamp from navigation-to-Dashboard to the
   final composed frame including chart draw, on a populated database.

Both platforms (Android device/emulator and Desktop) should be
measured separately — they use different SQL drivers (ADR 0005) and
have historically diverged behavior in this project (Desktop is
exercised by real integration tests; Android's encrypted-driver path
has never run on real hardware in this environment).

## Baselines

None. This is the first performance-measurement pass attempted for
this project; there is no prior release to compare against.

## Results

**Not yet measured**, for all three targets, on both platforms. This
is the top-ranked item in `project-memory-bank/26-active-initiatives.md`
and in `documents/00-leverage-strategy.md`'s 30-day plan (Week 2).

## Conclusions

The targets are **structurally justified**, not empirically confirmed:

- Cold start: the database is pre-warmed at startup (Phase 10) and the
  schema carries indexes on the columns the Dashboard/recent-activity
  queries filter on.
- Search: FTS5 is a purpose-built indexed search structure; the
  concern is dataset-size scaling, not algorithmic complexity, and
  that scaling behavior specifically has not been tested against a
  large synthetic dataset.
- Dashboard: the recent-transactions query is explicitly bounded
  (Phase 10), avoiding an unbounded table scan as data grows.

Until real measurements exist, treat these targets as engineering
intent, not a verified SLA. Any external-facing material (README,
blog posts, talk decks) referencing performance must say "targeted,"
not "achieved" — this report is the source of truth for that
distinction, and it should be updated with real numbers the first time
a measurement pass actually runs.
