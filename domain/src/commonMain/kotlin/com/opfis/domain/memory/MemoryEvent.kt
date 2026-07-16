package com.opfis.domain.memory

import com.opfis.domain.entity.EntityRef
import kotlinx.serialization.Serializable

/**
 * A single entry in the app's persistent financial memory timeline
 * (ROADMAP Phase 6). Manually recorded by the user - a free-form
 * [NOTE][MemoryEventType.NOTE] or a [MILESTONE][MemoryEventType.MILESTONE]
 * worth remembering - optionally attached to one existing [subject]
 * entity (e.g. an Account or a Goal). Automatic event generation from
 * other use cases (e.g. "budget exceeded") is deliberately out of
 * scope for this phase - see `13-memory-engine.md`.
 */
@Serializable
data class MemoryEvent(
    val id: String,
    val eventType: MemoryEventType,
    val title: String,
    val description: String,
    val subject: EntityRef?,
    val occurredAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(title.isNotBlank()) { "title must not be blank" }
    }
}

@Serializable
enum class MemoryEventType {
    NOTE,
    MILESTONE,
}
