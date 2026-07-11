package com.opfis.data.category

import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryType
import com.opfis.data.db.Category as CategoryRow

internal fun toDomainCategory(row: CategoryRow): Category =
    Category(
        id = row.id,
        name = row.name,
        type = CategoryType.valueOf(row.type),
        parentId = row.parent_id,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
    )
