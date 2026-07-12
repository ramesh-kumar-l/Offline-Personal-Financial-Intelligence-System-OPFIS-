package com.opfis.data.relationship

import com.opfis.data.testDatabase
import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import com.opfis.domain.relationship.Relationship
import com.opfis.domain.relationship.RelationshipType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SqlRelationshipRepositoryTest {
    @Test
    fun `upsert persists a relationship`() =
        runTest {
            val repository = SqlRelationshipRepository(testDatabase())
            val relationship =
                Relationship(
                    id = "rel-1",
                    from = EntityRef(EntityType.DOCUMENT, "doc-1"),
                    to = EntityRef(EntityType.LIABILITY, "liab-1"),
                    relationshipType = RelationshipType.SUPPORTING_DOCUMENT,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                )

            repository.upsert(relationship)

            assertEquals(relationship, repository.observeInvolving(EntityType.LIABILITY, "liab-1").first().single())
        }

    @Test
    fun `observeInvolving finds a relationship from either endpoint`() =
        runTest {
            val repository = SqlRelationshipRepository(testDatabase())
            val relationship =
                Relationship(
                    id = "rel-1",
                    from = EntityRef(EntityType.DOCUMENT, "doc-1"),
                    to = EntityRef(EntityType.LIABILITY, "liab-1"),
                    relationshipType = RelationshipType.SUPPORTING_DOCUMENT,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                )
            repository.upsert(relationship)

            val fromDocument = repository.observeInvolving(EntityType.DOCUMENT, "doc-1").first()
            val fromLiability = repository.observeInvolving(EntityType.LIABILITY, "liab-1").first()

            assertEquals(relationship, fromDocument.single())
            assertEquals(relationship, fromLiability.single())
        }

    @Test
    fun `delete removes the relationship`() =
        runTest {
            val repository = SqlRelationshipRepository(testDatabase())
            val relationship =
                Relationship(
                    id = "rel-1",
                    from = EntityRef(EntityType.DOCUMENT, "doc-1"),
                    to = EntityRef(EntityType.LIABILITY, "liab-1"),
                    relationshipType = RelationshipType.SUPPORTING_DOCUMENT,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                )
            repository.upsert(relationship)

            repository.delete("rel-1")

            assertNull(repository.observeInvolving(EntityType.DOCUMENT, "doc-1").first().firstOrNull())
        }
}
