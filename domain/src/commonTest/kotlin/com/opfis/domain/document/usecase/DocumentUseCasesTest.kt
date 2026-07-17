package com.opfis.domain.document.usecase

import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentRepository
import com.opfis.domain.document.DocumentStoragePort
import com.opfis.domain.document.DocumentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeDocumentQueryRepository(
    private val documents: List<Document> = emptyList(),
) : DocumentRepository {
    val deleted = mutableListOf<String>()
    val linked = mutableListOf<Pair<String, String?>>()

    override fun observeAll(): Flow<List<Document>> = flowOf(documents)

    override fun observeByTransaction(transactionId: String): Flow<List<Document>> =
        flowOf(documents.filter { it.linkedTransactionId == transactionId })

    override suspend fun upsert(document: Document): Unit = error("not used in this test")

    override suspend fun delete(id: String) {
        deleted.add(id)
    }

    override suspend fun linkToTransaction(
        documentId: String,
        transactionId: String?,
    ) {
        linked.add(documentId to transactionId)
    }
}

private class FakeDocumentDeleteStoragePort : DocumentStoragePort {
    val deleted = mutableListOf<String>()

    override suspend fun save(
        id: String,
        fileName: String,
        bytes: ByteArray,
    ): String = error("not used in this test")

    override suspend fun read(storagePath: String): ByteArray = error("not used in this test")

    override suspend fun delete(storagePath: String) {
        deleted.add(storagePath)
    }
}

class DocumentUseCasesTest {
    private val document =
        Document(
            "doc-1",
            "receipt.pdf",
            "/path/receipt.pdf",
            "application/pdf",
            DocumentType.RECEIPT,
            "",
            linkedTransactionId = "tx-1",
            importedAt = 0L,
            createdAt = 0L,
            updatedAt = 0L,
        )

    @Test
    fun `observe documents returns the repository stream`() =
        runTest {
            val useCase = ObserveDocumentsUseCase(FakeDocumentQueryRepository(listOf(document)))
            assertEquals(listOf(document), useCase().first())
        }

    @Test
    fun `observe documents for transaction filters by linked transaction`() =
        runTest {
            val useCase = ObserveDocumentsForTransactionUseCase(FakeDocumentQueryRepository(listOf(document)))
            assertEquals(listOf(document), useCase("tx-1").first())
        }

    @Test
    fun `link document to transaction delegates to the repository`() =
        runTest {
            val repository = FakeDocumentQueryRepository()
            LinkDocumentToTransactionUseCase(repository)("doc-1", "tx-2")
            assertEquals(listOf<Pair<String, String?>>("doc-1" to "tx-2"), repository.linked)
        }

    @Test
    fun `delete document removes both the repository row and the stored file`() =
        runTest {
            val repository = FakeDocumentQueryRepository()
            val storage = FakeDocumentDeleteStoragePort()
            DeleteDocumentUseCase(repository, storage)("doc-1", "/path/receipt.pdf")
            assertEquals(listOf("doc-1"), repository.deleted)
            assertEquals(listOf("/path/receipt.pdf"), storage.deleted)
        }
}
