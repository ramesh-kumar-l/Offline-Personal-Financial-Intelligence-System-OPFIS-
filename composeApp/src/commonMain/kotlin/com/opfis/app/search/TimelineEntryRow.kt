package com.opfis.app.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.app.format.DateFormatter
import com.opfis.app.format.MoneyFormatter
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.tag.Tag
import com.opfis.domain.timeline.TimelineEntry
import com.opfis.domain.transaction.TransactionType

/**
 * One timeline row (ROADMAP Phase 4, "Timeline search" + "Tags"):
 * transaction summary plus its assigned tag chips and a menu to attach
 * any tag not already on it.
 */
@Composable
internal fun TimelineEntryRow(
    entry: TimelineEntry,
    tags: List<Tag>,
    onAssignTag: (transactionId: String, tagId: String) -> Unit,
    onRemoveTag: (transactionId: String, tagId: String) -> Unit,
) {
    val transaction = entry.transaction
    val (marker, tint, sign) =
        when (transaction.type) {
            TransactionType.INCOME -> Triple("▲", OpfisColors.Success, "+")
            TransactionType.EXPENSE -> Triple("▼", OpfisColors.Error, "-")
            TransactionType.TRANSFER -> Triple("⇄", OpfisColors.InformationNeutralBlue, "")
        }
    val description =
        transaction.description.ifBlank {
            transaction.type.name
                .lowercase()
                .replaceFirstChar(Char::uppercase)
        }
    val assignedTags = tags.filter { it.id in entry.tagIds }
    val availableTags = tags.filterNot { it.id in entry.tagIds }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("$marker $description", style = MaterialTheme.typography.bodyLarge, color = tint)
                Text(DateFormatter.formatDay(transaction.occurredAt), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "$sign${MoneyFormatter.format(transaction.amountMinorUnits)}",
                style = MaterialTheme.typography.bodyLarge,
                color = tint,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            assignedTags.forEach { tag ->
                TextButton(onClick = { onRemoveTag(transaction.id, tag.id) }) {
                    Text("#${tag.name} ✕", style = MaterialTheme.typography.bodySmall, color = OpfisColors.Warning)
                }
            }
            if (availableTags.isNotEmpty()) {
                TextButton(onClick = { menuExpanded = true }) {
                    Text("+ tag", style = MaterialTheme.typography.bodySmall)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    availableTags.forEach { tag ->
                        DropdownMenuItem(
                            text = { Text(tag.name) },
                            onClick = {
                                onAssignTag(transaction.id, tag.id)
                                menuExpanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}
