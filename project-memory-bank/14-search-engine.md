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

## Phase 5 - document search (implemented)

`search_index` gained a `document`/`extracted_text` source: the
`document_search_ai`/`document_search_ad` triggers (`migrations/4.sqm`)
fold `file_name || ' ' || extracted_text` into the FTS5 table under
`entity_type = 'DOCUMENT'` on import/delete, using the same
delete-then-insert `AFTER INSERT` pattern as every other source table.
`SearchResult` gained `DocumentMatch`; no changes were needed to
`SqlSearchIndexRepository`, `FtsQueryBuilder`, or
`SearchFinancialRecordsUseCase` - `DOCUMENT` is just another
`SearchEntityType` value flowing through the existing per-entity-type
query/union/`SearchResult`-mapping path. See `16-document-engine.md`
for how `extractedText` itself is produced (PDF-text/OCR).

## Phase 6 - memory event search (implemented)

`search_index` gained a `memory_event`/`title`+`description` source,
via the same trigger pattern as every other content-bearing table.
`SearchResult` gained `MemoryEventMatch`; `SqlSearchIndexRepository`
gained a sixth `searchMemoryEvents` branch, and since
`kotlinx.coroutines.flow.combine` only has typed-tuple overloads up to
5 flows, the 6-source `combine` call switched to the vararg-array
overload (`combine(f1, ..., f6) { results: Array<List<SearchResult>> ->
results.toList().flatten() }`). `Relationship` (also Phase 6) is
deliberately not search-indexed - it has no free text, only typed
entity references. See `13-memory-engine.md`.