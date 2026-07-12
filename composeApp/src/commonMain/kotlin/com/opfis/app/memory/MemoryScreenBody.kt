package com.opfis.app.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventType

/** Renders [MemoryScreen]'s layout: a note/milestone entry form and the chronological timeline below it. */
@Composable
internal fun MemoryScreenBody(
    padding: PaddingValues,
    memoryEvents: List<MemoryEvent>,
    onRecordEvent: RecordEventCallback,
    onDeleteEvent: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RecordMemoryEventForm(onRecordEvent = onRecordEvent)

        if (memoryEvents.isEmpty()) {
            Text("No memories recorded yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            memoryEvents.forEach { event ->
                MemoryEventRow(event = event, onDelete = { onDeleteEvent(event.id) })
            }
        }
    }
}

@Composable
private fun RecordMemoryEventForm(onRecordEvent: RecordEventCallback) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf(MemoryEventType.NOTE) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Record a memory", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MemoryEventType.entries.forEach { type ->
                    TextButton(onClick = { eventType = type }) {
                        Text(if (eventType == type) "● ${type.name}" else type.name)
                    }
                }
            }
            TextButton(
                onClick = {
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.isNotEmpty()) {
                        onRecordEvent(trimmedTitle, description.trim(), eventType)
                        title = ""
                        description = ""
                    }
                },
            ) {
                Text("Save")
            }
        }
    }
}
