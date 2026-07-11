package com.opfis.data.tag

import com.opfis.data.testDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SqlTransactionTagRepositoryTest {
    @Test
    fun `assign then observe reflects the tag on its transaction`() =
        runTest {
            val repository = SqlTransactionTagRepository(testDatabase())

            repository.assignTag("tx-1", "tag-groceries")

            val assignments = repository.observeTagIdsByTransaction().first()

            assertEquals(listOf("tag-groceries"), assignments["tx-1"])
        }

    @Test
    fun `assigning the same tag twice does not duplicate it`() =
        runTest {
            val repository = SqlTransactionTagRepository(testDatabase())

            repository.assignTag("tx-1", "tag-groceries")
            repository.assignTag("tx-1", "tag-groceries")

            assertEquals(listOf("tag-groceries"), repository.observeTagIdsByTransaction().first()["tx-1"])
        }

    @Test
    fun `unassign removes the tag from the transaction`() =
        runTest {
            val repository = SqlTransactionTagRepository(testDatabase())
            repository.assignTag("tx-1", "tag-groceries")

            repository.unassignTag("tx-1", "tag-groceries")

            assertNull(repository.observeTagIdsByTransaction().first()["tx-1"])
        }
}
