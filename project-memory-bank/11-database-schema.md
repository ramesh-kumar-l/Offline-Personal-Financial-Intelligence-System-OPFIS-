# Database Schema

SQLDelight `.sq` files live in `data/src/commonMain/sqldelight/com/opfis/data/db/`;
migrations in `data/src/commonMain/sqldelight/migrations/<fromVersion>.sqm`
(auto-applied on open by the schema-aware driver factory - ADR 0005).

Every table follows the SYSTEM_PROMPT Part 2 audit-metadata rule:
`created_at INTEGER`, `updated_at INTEGER`, `version INTEGER DEFAULT 1`
(incremented by the repository on each write), in addition to a primary
key.

## Schema versions

- **v1** (`SystemStatus.sq`): `system_status_indicator` (Phase 0).
- **v2** (`migrations/1.sqm`): adds `version` column to
  `system_status_indicator` (Phase 1).
- **v3** (`migrations/2.sqm`): adds the Phase 2 financial-domain tables
  below.
- **v4** (`migrations/3.sqm`): adds `tag`, `transaction_tag`, and the
  `search_index` FTS5 virtual table (Phase 4).
- **v5** (`migrations/4.sqm`): adds `document` (Phase 5).
- **v6** (`migrations/5.sqm`): adds `memory_event` and `relationship`
  (Phase 6).
- **v7** (`migrations/6.sqm`): adds `audit_log` (Phase 8) - missing from
  this list until now, a documentation gap predating Phase 10, filled
  in while adding the v8 entry below.
- **v8** (`migrations/7.sqm`): adds two indexes to `financial_transaction`
  - `occurred_at` and `transfer_account_id` (Phase 10 query
  optimization, see `20-performance-budget.md`). No new tables.

## Phase 2 tables

| Table | Key columns | Notes |
|---|---|---|
| `account` | `balance_minor_units`, `is_archived` | balance mutated only via ledger posting |
| `asset` | `value_minor_units` | independent of `account` |
| `liability` | `balance_minor_units`, `interest_rate_basis_points` | independent of `account` |
| `category` | `parent_id` | self-referential, one or more levels of nesting |
| `budget` | `category_id`, `limit_minor_units`, `period` | definition only, no spend tracking |
| `goal` | `target_amount_minor_units`, `current_amount_minor_units` | progress is user-editable |
| `financial_transaction` | `account_id`, `transfer_account_id`, `amount_minor_units`, `type` | indexed on `account_id`, `transfer_account_id`, and `occurred_at` (the last two added in Phase 10); named `financial_transaction` (not `transaction`) to avoid colliding with SQLDelight's generated `Transaction` class and its own `transaction {}` API |

No `PRAGMA foreign_keys` enforcement is enabled - referential columns
(`parent_id`, `category_id`, `account_id`, `transfer_account_id`) are
plain columns, not declared FK constraints, so the schema does not
imply integrity it doesn't enforce (see `12-financial-engine.md`).

## Phase 4 tables

| Table | Key columns | Notes |
|---|---|---|
| `tag` | `name`, `color_hex` | client-generated `kotlin.uuid.Uuid` id |
| `transaction_tag` | `transaction_id`, `tag_id` | many-to-many join |
| `search_index` | `entity_type` (UNINDEXED), `entity_id` (UNINDEXED), `text` | FTS5 virtual table; kept in sync via `AFTER INSERT`/`AFTER DELETE` triggers on account/category/financial_transaction/tag - see `14-search-engine.md` |

## Phase 5 tables

| Table | Key columns | Notes |
|---|---|---|
| `document` | `storage_path`, `mime_type`, `document_type`, `extracted_text` (default `''`), `linked_transaction_id` (indexed) | file bytes live outside the DB (`DocumentStoragePort`); `document_search_ai`/`document_search_ad` triggers fold `file_name \|\| ' ' \|\| extracted_text` into `search_index` under `entity_type = 'DOCUMENT'` |

## Phase 6 tables

| Table | Key columns | Notes |
|---|---|---|
| `memory_event` | `event_type`, `title`, `description` (default `''`), `subject_entity_type`/`subject_entity_id` (both nullable, indexed together) | `memory_event_search_ai`/`memory_event_search_ad` triggers fold `title \|\| ' ' \|\| description` into `search_index` under `entity_type = 'MEMORY_EVENT'` |
| `relationship` | `from_entity_type`/`from_entity_id` (indexed), `to_entity_type`/`to_entity_id` (indexed), `relationship_type` | not search-indexed - no free text; SQLDelight generated its row class as `Relationship` (single-word table name), unlike the multi-word `Memory_event`/`Financial_transaction`/`Transaction_tag` (see `05-current-state.md`) |
