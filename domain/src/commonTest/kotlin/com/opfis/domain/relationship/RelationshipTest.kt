package com.opfis.domain.relationship

import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RelationshipTest {
    @Test
    fun `a relationship cannot link an entity to itself`() {
        val ref = EntityRef(EntityType.DOCUMENT, "doc-1")

        assertFailsWith<IllegalArgumentException> {
            Relationship(
                id = "rel-1",
                from = ref,
                to = ref,
                relationshipType = RelationshipType.RELATED,
                createdAt = 0L,
                updatedAt = 0L,
            )
        }
    }
}
