package com.opfis.app.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.opfis.domain.search.SearchEntityType
import com.opfis.domain.search.SearchFilter

/** Entity-type toggle chips narrowing global search (ROADMAP Phase 4, "Filters"). */
@Composable
fun SearchFilterBar(
    filter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SearchEntityType.entries.forEach { type ->
            val selected = type in filter.entityTypes
            FilterChip(
                selected = selected,
                onClick = {
                    val updated = if (selected) filter.entityTypes - type else filter.entityTypes + type
                    onFilterChange(filter.copy(entityTypes = updated))
                },
                label = { Text(type.name.lowercase().replaceFirstChar(Char::uppercase)) },
            )
        }
    }
}
