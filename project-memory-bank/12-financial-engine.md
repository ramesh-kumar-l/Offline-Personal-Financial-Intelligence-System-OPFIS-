# Financial Engine

Phase 2 delivers the core financial engine: Accounts, Assets,
Liabilities, Categories, Transactions, Budgets, Goals, plus the ledger
logic that keeps account balances consistent with posted transactions.
See `03-domain-model.md` for entity field lists.

## Ledger posting (the actual "engine")

`TransactionLedgerRules.accountDeltas(transaction)` (pure, unit-tested
in `domain/src/commonTest`) is the single place the sign convention
lives:

- INCOME: `+amount` to `accountId`
- EXPENSE: `-amount` to `accountId`
- TRANSFER: `-amount` to `accountId`, `+amount` to `transferAccountId`

`SqlFinancialLedger` (`:data`, implements `FinancialLedgerPort`) applies
the transaction row write and every resulting balance delta inside one
SQLDelight `transaction {}` block, so a crash mid-write cannot leave an
account balance out of sync with its transaction history (SYSTEM_PROMPT
Part 2: writes must be atomic/transactional/recoverable). Deleting a
transaction re-reads it, removes the row, and applies
`TransactionLedgerRules.reversalDeltas` in the same atomic block.

## Net worth / budget analytics

Net worth computation (accounts + assets - liabilities) and
budget-spend-vs-limit analytics are explicitly **not** part of Phase 2 -
they are Dashboard/UX work (ROADMAP Phase 3), which will read the Phase
2 repositories rather than duplicate their data.

## Not yet implemented

- ID generation for new entities (Presentation layer / Phase 3 UI
  responsibility - Phase 2 use cases accept fully-formed entities).
- Multi-currency / FX conversion.
- FK-level referential integrity in SQLite (no `PRAGMA foreign_keys`
  enforcement is enabled anywhere in the schema yet; parent/account
  references are plain columns, intentionally not declared as
  unenforced FK constraints to avoid a misleading appearance of
  integrity).
