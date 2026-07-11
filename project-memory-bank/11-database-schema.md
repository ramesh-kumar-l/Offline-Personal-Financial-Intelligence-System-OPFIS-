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

## Phase 2 tables

| Table | Key columns | Notes |
|---|---|---|
| `account` | `balance_minor_units`, `is_archived` | balance mutated only via ledger posting |
| `asset` | `value_minor_units` | independent of `account` |
| `liability` | `balance_minor_units`, `interest_rate_basis_points` | independent of `account` |
| `category` | `parent_id` | self-referential, one or more levels of nesting |
| `budget` | `category_id`, `limit_minor_units`, `period` | definition only, no spend tracking |
| `goal` | `target_amount_minor_units`, `current_amount_minor_units` | progress is user-editable |
| `financial_transaction` | `account_id`, `transfer_account_id`, `amount_minor_units`, `type` | indexed on `account_id`; named `financial_transaction` (not `transaction`) to avoid colliding with SQLDelight's generated `Transaction` class and its own `transaction {}` API |

No `PRAGMA foreign_keys` enforcement is enabled - referential columns
(`parent_id`, `category_id`, `account_id`, `transfer_account_id`) are
plain columns, not declared FK constraints, so the schema does not
imply integrity it doesn't enforce (see `12-financial-engine.md`).
