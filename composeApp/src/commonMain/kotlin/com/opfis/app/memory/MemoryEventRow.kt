package com.opfis.app.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventType

/** One entry in the financial memory timeline: a "★" milestone or a "✎" free-form note. */
@Composable
internal fun MemoryEventRow(
    event: MemoryEvent,
    onDelete: () -> Unit,
) {
    val glyph = if (event.eventType == MemoryEventType.MILESTONE) "★" else "✎"
    val glyphColor =
        if (event.eventType == MemoryEventType.MILESTONE) OpfisColors.Warning else OpfisColors.InformationNeutralBlue

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("$glyph ${event.title}", style = MaterialTheme.typography.bodyLarge, color = glyphColor)
            if (event.description.isNotBlank()) {
                Text(event.description, style = MaterialTheme.typography.bodySmall)
            }
        }
        TextButton(onClick = onDelete) {
            Text("Delete", color = OpfisColors.Error)
        }
    }
}
