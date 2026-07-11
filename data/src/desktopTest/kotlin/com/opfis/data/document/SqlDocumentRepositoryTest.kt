package com.opfis.data.document

import com.opfis.data.testDatabase
import com.opfis.domain.document.Document
import com.opfis.domain.document.DocumentType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun testDocument(
    id: String = "doc-1",
    linkedTransactionId: String? = null,
) = Document(
    id = id,
    fileName = "receipt.pdf",
    storagePath = "/documents/$id.pdf",
    mimeType = "application/pdf",
    documentType = DocumentType.RECEIPT,
    extractedText = "Total: 42.00",
    linkedTransactionId = linkedTransactionId,
    importedAt = 0L,
    createdAt = 0L,
    updatedAt = 0L,
)

class SqlDocumentRepositoryTest {
    @Test
    fun `upsert persists a document`() =
        runTest {
            val repository = SqlDocumentRepository(testDatabase())
            repository.upsert(testDocument())

            val documents = repository.observeAll().first()

            assertEquals("receipt.pdf", documents.single().fileName)
        }

    @Test
    fun `observeByTransaction returns only linked documents`() =
        runTest {
            val repository = SqlDocumentRepository(testDatabase())
            repository.upsert(testDocument(id = "doc-1", linkedTransactionId = "tx-1"))
            repository.upsert(testDocument(id = "doc-2", linkedTransactionId = null))

            val linked = repository.observeByTransaction("tx-1").first()

            assertEquals("doc-1", linked.single().id)
        }

    @Test
    fun `linkToTransaction updates the linked id without touching other fields`() =
        runTest {
            val repository = SqlDocumentRepository(testDatabase())
            repository.upsert(testDocument())

            repository.linkToTransaction("doc-1", "tx-1")

            val document = repository.observeAll().first().single()
            assertEquals("tx-1", document.linkedTransactionId)
            assertEquals("Total: 42.00", document.extractedText)
        }

    @Test
    fun `delete removes the document`() =
        runTest {
            val repository = SqlDocumentRepository(testDatabase())
            repository.upsert(testDocument())

            repository.delete("doc-1")

            assertNull(repository.observeAll().first().firstOrNull())
        }
}
