# Search Engine

## Phase 4 - FTS5-backed global search (implemented)

`search_index` is a SQLite FTS5 virtual table
(`entity_type UNINDEXED, entity_id UNINDEXED, text`) kept in sync with
`account`/`category`/`financial_transaction`/`tag` via one `AFTER
INSERT` (delete-then-insert, since the app only ever uses `INSERT OR
REPLACE`) and one `AFTER DELETE` trigger per source table - no triggers
depend on a real `UPDATE` statement. `SqlSearchIndexRepository`
(`:data/.../search/`) implements the `SearchPort` domain port, running
one `MATCH`/`bm25()`-ranked query per requested `SearchEntityType` and
merging results client-side. **Hard rule**: `MATCH` and `bm25()` must
reference the virtual table's real schema name (`search_index`), never
a query alias - SQLDelight's SQLite dialect parser rejects the alias
form (`No column found with name <alias>`), even though the ordinary
`JOIN ... ON alias.col = ...` condition can use the alias freely.

`SearchFinancialRecordsUseCase` (`domain/.../search/usecase/`) fans a
query `Flow<String>` and a `SearchFilter` `Flow` into
`SearchPort.search()` via `combine` + `flatMapLatest` (replaces Phase
3's `FinancialSearchEngine`, which is deleted). `SearchFilter`'s
`tagIds`/date-range fields deliberately apply only to
`ObserveTimelineUseCase`'s chronological browse (the blank-query
Timeline section), not to FTS5 global text search - text relevance and
record browsing are treated as separate concerns by design.

Phase 5 will extend `search_index` with a `document`/`extracted_text`
source (see `05-current-state.md` for status) using the same
trigger-based sync pattern.