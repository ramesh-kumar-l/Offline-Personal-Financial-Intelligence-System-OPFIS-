# Search Engine
SQLite FTS5 + optional vector index abstraction. (Phase 4 scope.)

## Phase 3 interim search entry

`FinancialSearchEngine` (`domain/.../search/`) is a deliberately
minimal, real (not stubbed) in-memory case-insensitive substring search
over Account names, Category names, and Transaction descriptions -
enough to give the Dashboard's search bar genuine functionality without
doing Phase 4's work early. `SearchFinancialRecordsUseCase` combines the
three repository `Flow`s plus a query `Flow<String>`. Phase 4 replaces
this object with FTS5-backed ranked/filtered search; the use case's
public surface is expected to stay stable across that swap.