package com.opfis.domain.document.usecase

import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentRepository
import com.opfis.domain.document.DocumentStoragePort
import com.opfis.domain.document.DocumentTextExtractorPort
import com.opfis.domain.document.DocumentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeDocumentRepository : DocumentRepository {
    val saved = mutableListOf<Document>()

    override fun observeAll(): Flow<List<Document>> = MutableStateFlow(saved)

    override fun observeByTransaction(transactionId: String): Flow<List<Document>> = MutableStateFlow(emptyList())

    override suspend fun upsert(document: Document) {
        saved.add(document)
    }

    override suspend fun delete(id: String) {
        saved.removeAll { it.id == id }
    }

    override suspend fun linkToTransaction(
        documentId: String,
        transactionId: String?,
    ) {
        val index = saved.indexOfFirst { it.id == documentId }
        saved[index] = saved[index].copy(linkedTransactionId = transactionId)
    }
}

private class FakeDocumentStoragePort : DocumentStoragePort {
    override suspend fun save(
        id: String,
        fileName: String,
        bytes: ByteArray,
    ): String = "/storage/$id-$fileName"

    override suspend fun read(storagePath: String): ByteArray = ByteArray(0)

    override suspend fun delete(storagePath: String) = Unit
}

private class FakeDocumentTextExtractorPort(
    private val text: String,
) : DocumentTextExtractorPort {
    override suspend fun extractText(
        bytes: ByteArray,
        mimeType: String,
    ): String = text
}

class ImportDocumentUseCaseTest {
    @Test
    fun `saves the file, extracts its text, and persists the document`() =
        runTest {
            val repository = FakeDocumentRepository()
            val useCase =
                ImportDocumentUseCase(
                    documentRepository = repository,
                    storage = FakeDocumentStoragePort(),
                    textExtractor = FakeDocumentTextExtractorPort("Total: $42.00"),
                )

            val document =
                useCase(
                    ImportDocumentRequest(
                        id = "doc-1",
                        fileName = "receipt.png",
                        bytes = ByteArray(4),
                        mimeType = "image/png",
                        documentType = DocumentType.RECEIPT,
                        linkedTransactionId = "tx-1",
                        now = 1_000L,
                    ),
                )

            assertEquals("/storage/doc-1-receipt.png", document.storagePath)
            assertEquals("Total: $42.00", document.extractedText)
            assertEquals("tx-1", document.linkedTransactionId)
            assertEquals(document, repository.saved.single())
        }
}
