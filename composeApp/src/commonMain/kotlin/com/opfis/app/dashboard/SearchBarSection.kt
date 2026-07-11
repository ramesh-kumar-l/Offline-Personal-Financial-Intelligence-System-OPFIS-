package com.opfis.app.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.app.format.MoneyFormatter
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.search.SearchResult

/**
 * Dashboard search entry point (ROADMAP Phase 3: "Search entry").
 * Deliberately simple, in-memory substring matching - global full-text
 * search with filters/tags/timeline is Phase 4 ("Search") scope, see
 * `14-search-engine.md`.
 */
@Composable
fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<SearchResult>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search accounts, categories, transactions") },
            singleLine = true,
        )

        if (query.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (results.isEmpty()) {
                        Text("No results for \"$query\"", style = MaterialTheme.typography.bodySmall)
                    } else {
                        results.forEach { result -> SearchResultRow(result) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(result: SearchResult) {
    val (label, tint) =
        when (result) {
            is SearchResult.AccountMatch -> "Account: ${result.account.name}" to OpfisColors.ProfessionalBlue
            is SearchResult.CategoryMatch -> "Category: ${result.category.name}" to OpfisColors.InformationNeutralBlue
            is SearchResult.TransactionMatch ->
                "Transaction: ${result.transaction.description} (${MoneyFormatter.format(
                    result.transaction.amountMinorUnits,
                )})" to
                    OpfisColors.ProfessionalBlue
        }
    Text(label, style = MaterialTheme.typography.bodyMedium, color = tint)
}
