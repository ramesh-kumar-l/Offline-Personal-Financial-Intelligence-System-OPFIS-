package com.opfis.data.tag

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.tag.TransactionTagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SqlTransactionTagRepository(
    private val database: OpfisDatabase,
) : TransactionTagRepository {
    override fun observeTagIdsByTransaction(): Flow<Map<String, List<String>>> =
        database.transactionTagQueries
            .selectAllAssignments()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.groupBy({ it.transaction_id }, { it.tag_id }) }

    override suspend fun assignTag(
        transactionId: String,
        tagId: String,
    ) {
        database.transactionTagQueries.assign(
            transaction_id = transactionId,
            tag_id = tagId,
            created_at = Clock.System.now().toEpochMilliseconds(),
        )
    }

    override suspend fun unassignTag(
        transactionId: String,
        tagId: String,
    ) {
        database.transactionTagQueries.unassign(transactionId, tagId)
    }
}
