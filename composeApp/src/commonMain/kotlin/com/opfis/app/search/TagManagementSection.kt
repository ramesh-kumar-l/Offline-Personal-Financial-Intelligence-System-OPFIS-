package com.opfis.app.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.tag.Tag

/** Tag CRUD (ROADMAP Phase 4, "Tags") - create new tags, remove existing ones. */
@Composable
fun TagManagementSection(
    tags: List<Tag>,
    onCreateTag: (String) -> Unit,
    onDeleteTag: (String) -> Unit,
) {
    var newTagName by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tags", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("New tag") },
                    singleLine = true,
                )
                TextButton(
                    onClick = {
                        val trimmed = newTagName.trim()
                        if (trimmed.isNotEmpty()) {
                            onCreateTag(trimmed)
                            newTagName = ""
                        }
                    },
                ) {
                    Text("Add")
                }
            }

            if (tags.isEmpty()) {
                Text("No tags yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                tags.forEach { tag ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${tag.name}", style = MaterialTheme.typography.bodyMedium, color = OpfisColors.Warning)
                        TextButton(onClick = { onDeleteTag(tag.id) }) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}
