package com.opfis.domain.tag

import kotlinx.coroutines.flow.Flow

/**
 * Read/write port for the transaction<->tag many-to-many join. Exposed
 * as a single reactive map (transaction id -> its tag ids) rather than
 * per-transaction queries, so both "which tags does this transaction
 * have" and "which transactions carry this tag" (filtering) are served
 * from one flow without N+1 subscriptions.
 */
interface TransactionTagRepository {
    fun observeTagIdsByTransaction(): Flow<Map<String, List<String>>>

    suspend fun assignTag(
        transactionId: String,
        tagId: String,
    )

    suspend fun unassignTag(
        transactionId: String,
        tagId: String,
    )
}
