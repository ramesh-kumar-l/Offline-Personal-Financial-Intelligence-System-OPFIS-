package com.opfis.app.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.domain.tag.Tag
import com.opfis.domain.timeline.TimelineEntry

/** Chronological, taggable transaction browse (ROADMAP Phase 4, "Timeline search"). */
@Composable
fun TimelineSection(
    entries: List<TimelineEntry>,
    tags: List<Tag>,
    onAssignTag: (transactionId: String, tagId: String) -> Unit,
    onRemoveTag: (transactionId: String, tagId: String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Timeline", style = MaterialTheme.typography.titleMedium)

            if (entries.isEmpty()) {
                Text("No transactions recorded yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                entries.forEach { entry ->
                    TimelineEntryRow(entry = entry, tags = tags, onAssignTag = onAssignTag, onRemoveTag = onRemoveTag)
                }
            }
        }
    }
}
