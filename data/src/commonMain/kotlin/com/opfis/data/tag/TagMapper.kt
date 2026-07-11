package com.opfis.data.tag

import com.opfis.domain.tag.Tag
import com.opfis.data.db.Tag as TagRow

internal fun toDomainTag(row: TagRow): Tag =
    Tag(
        id = row.id,
        name = row.name,
        colorHex = row.color_hex,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
    )
