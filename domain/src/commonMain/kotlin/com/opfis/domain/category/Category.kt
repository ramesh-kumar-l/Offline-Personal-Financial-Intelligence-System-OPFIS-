package com.opfis.domain.category

/**
 * A transaction category, optionally nested under [parentId] (e.g.
 * "Food" -> "Groceries"). [type] constrains which
 * [com.opfis.domain.transaction.TransactionType] it may be applied to.
 */
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val parentId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(parentId != id) { "A category cannot be its own parent" }
    }
}

enum class CategoryType {
    INCOME,
    EXPENSE,
}
