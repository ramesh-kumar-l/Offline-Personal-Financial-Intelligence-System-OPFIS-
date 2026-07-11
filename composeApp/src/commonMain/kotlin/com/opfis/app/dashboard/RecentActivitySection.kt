package com.opfis.app.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.app.format.DateFormatter
import com.opfis.app.format.MoneyFormatter
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType

/**
 * "Recent Activity" dashboard widget (SystemPrompt Part 3,
 * "Home Dashboard"). Each row pairs a marker glyph, a color, and a text
 * label for its transaction type - color is never the sole signal.
 */
@Composable
fun RecentActivitySection(transactions: List<Transaction>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Recent Activity", style = MaterialTheme.typography.titleMedium)

            if (transactions.isEmpty()) {
                Text("No transactions recorded yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                transactions.forEach { transaction -> TransactionRow(transaction) }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
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
                .replaceFirstChar { it.uppercase() }
        }

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
}
