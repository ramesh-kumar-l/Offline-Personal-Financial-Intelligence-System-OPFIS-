package com.opfis.data.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountRepository
import com.opfis.domain.account.AccountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.opfis.data.db.Account as AccountRow

class SqlAccountRepository(
    private val database: OpfisDatabase,
) : AccountRepository {
    override fun observeAll(): Flow<List<Account>> =
        database.accountQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomain) }

    override fun observeById(id: String): Flow<Account?> =
        database.accountQueries
            .selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { row -> row?.let(::toDomain) }

    override suspend fun upsert(account: Account) {
        val existingVersion =
            database.accountQueries
                .selectById(account.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.accountQueries.insertOrReplace(
            id = account.id,
            name = account.name,
            type = account.type.name,
            balance_minor_units = account.balanceMinorUnits,
            is_archived = if (account.isArchived) 1L else 0L,
            created_at = account.createdAt,
            updated_at = account.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.accountQueries.deleteById(id)
    }

    private fun toDomain(row: AccountRow): Account =
        Account(
            id = row.id,
            name = row.name,
            type = AccountType.valueOf(row.type),
            balanceMinorUnits = row.balance_minor_units,
            isArchived = row.is_archived == 1L,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
        )
}
