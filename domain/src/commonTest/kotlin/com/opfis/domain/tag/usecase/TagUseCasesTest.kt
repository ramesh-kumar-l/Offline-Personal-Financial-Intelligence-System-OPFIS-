package com.opfis.domain.tag.usecase

import com.opfis.domain.tag.Tag
import com.opfis.domain.tag.TagRepository
import com.opfis.domain.tag.TransactionTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTagRepository(
    private val tags: List<Tag> = emptyList(),
) : TagRepository {
    val upserted = mutableListOf<Tag>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<Tag>> = flowOf(tags)

    override suspend fun upsert(tag: Tag) {
        upserted.add(tag)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

private class FakeTransactionTagRepository(
    private val tagIdsByTransaction: Map<String, List<String>> = emptyMap(),
) : TransactionTagRepository {
    val assigned = mutableListOf<Pair<String, String>>()
    val unassigned = mutableListOf<Pair<String, String>>()

    override fun observeTagIdsByTransaction(): Flow<Map<String, List<String>>> = flowOf(tagIdsByTransaction)

    override suspend fun assignTag(
        transactionId: String,
        tagId: String,
    ) {
        assigned.add(transactionId to tagId)
    }

    override suspend fun unassignTag(
        transactionId: String,
        tagId: String,
    ) {
        unassigned.add(transactionId to tagId)
    }
}

class TagUseCasesTest {
    private val tag = Tag("tag-1", "Essential", createdAt = 0L, updatedAt = 0L)

    @Test
    fun `observe tags returns the repository stream`() =
        runTest {
            val useCase = ObserveTagsUseCase(FakeTagRepository(listOf(tag)))
            assertEquals(listOf(tag), useCase().first())
        }

    @Test
    fun `upsert tag delegates to the repository`() =
        runTest {
            val repository = FakeTagRepository()
            UpsertTagUseCase(repository)(tag)
            assertEquals(listOf(tag), repository.upserted)
        }

    @Test
    fun `delete tag delegates to the repository`() =
        runTest {
            val repository = FakeTagRepository()
            DeleteTagUseCase(repository)(tag.id)
            assertEquals(listOf(tag.id), repository.deleted)
        }

    @Test
    fun `observe transaction tags returns the repository stream`() =
        runTest {
            val useCase = ObserveTransactionTagsUseCase(FakeTransactionTagRepository(mapOf("tx-1" to listOf("tag-1"))))
            assertEquals(mapOf("tx-1" to listOf("tag-1")), useCase().first())
        }

    @Test
    fun `assign tag to transaction delegates to the repository`() =
        runTest {
            val repository = FakeTransactionTagRepository()
            AssignTagToTransactionUseCase(repository)("tx-1", "tag-1")
            assertEquals(listOf("tx-1" to "tag-1"), repository.assigned)
        }

    @Test
    fun `remove tag from transaction delegates to the repository`() =
        runTest {
            val repository = FakeTransactionTagRepository()
            RemoveTagFromTransactionUseCase(repository)("tx-1", "tag-1")
            assertEquals(listOf("tx-1" to "tag-1"), repository.unassigned)
        }
}
