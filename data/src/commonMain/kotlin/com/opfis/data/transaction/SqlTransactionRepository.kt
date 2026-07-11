package com.opfis.data.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlTransactionRepository(
    private val database: OpfisDatabase,
) : TransactionRepository {
    override fun observeAll(): Flow<List<Transaction>> =
        database.financialTransactionQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainTransaction) }

    override fun observeByAccount(accountId: String): Flow<List<Transaction>> =
        database.financialTransactionQueries
            .selectByAccount(accountId, accountId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainTransaction) }
}
