package com.opfis.domain.document

import kotlin.test.Test
import kotlin.test.assertFailsWith

class DocumentTest {
    @Test
    fun `a document cannot have a blank file name`() {
        assertFailsWith<IllegalArgumentException> {
            document(fileName = "   ")
        }
    }

    @Test
    fun `a document cannot have a blank storage path`() {
        assertFailsWith<IllegalArgumentException> {
            document(storagePath = "   ")
        }
    }

    private fun document(
        fileName: String = "receipt.pdf",
        storagePath: String = "/documents/receipt.pdf",
    ) = Document(
        id = "doc-1",
        fileName = fileName,
        storagePath = storagePath,
        mimeType = "application/pdf",
        documentType = DocumentType.RECEIPT,
        extractedText = "",
        importedAt = 0L,
        createdAt = 0L,
        updatedAt = 0L,
    )
}
