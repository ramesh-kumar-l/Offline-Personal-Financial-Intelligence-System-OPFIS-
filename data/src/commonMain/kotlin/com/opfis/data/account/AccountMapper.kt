package com.opfis.data.account

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountType
import com.opfis.data.db.Account as AccountRow

internal fun toDomainAccount(row: AccountRow): Account =
    Account(
        id = row.id,
        name = row.name,
        type = AccountType.valueOf(row.type),
        balanceMinorUnits = row.balance_minor_units,
        isArchived = row.is_archived == 1L,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
    )
