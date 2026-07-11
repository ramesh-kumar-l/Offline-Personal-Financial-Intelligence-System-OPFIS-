package com.opfis.app.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.opfis.domain.tag.Tag

/** Tag membership filter for the timeline browse (ROADMAP Phase 4, "Tags" + "Filters"). */
@Composable
fun TagFilterRow(
    tags: List<Tag>,
    selectedTagIds: Set<String>,
    onToggleTag: (String) -> Unit,
) {
    if (tags.isEmpty()) return

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tags, key = { it.id }) { tag ->
            val selected = tag.id in selectedTagIds
            FilterChip(
                selected = selected,
                onClick = { onToggleTag(tag.id) },
                label = { Text("#${tag.name}") },
            )
        }
    }
}
