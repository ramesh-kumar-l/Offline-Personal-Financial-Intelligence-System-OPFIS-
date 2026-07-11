package com.opfis.domain.tag

/**
 * A user-defined label attachable to transactions (ROADMAP Phase 4,
 * "Tags"), independent of [com.opfis.domain.category.Category]. Many
 * tags may apply to one transaction, and one tag to many transactions -
 * see [TransactionTagRepository].
 */
data class Tag(
    val id: String,
    val name: String,
    val colorHex: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
    }
}
