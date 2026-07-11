package com.opfis.data.transaction

import com.opfis.data.db.Financial_transaction
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType

internal fun toDomainTransaction(row: Financial_transaction): Transaction =
    Transaction(
        id = row.id,
        accountId = row.account_id,
        categoryId = row.category_id,
        type = TransactionType.valueOf(row.type),
        amountMinorUnits = row.amount_minor_units,
        transferAccountId = row.transfer_account_id,
        description = row.description,
        occurredAt = row.occurred_at,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
    )
