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
import com.opfis.app.search.SearchResultRow
import com.opfis.domain.search.SearchResult

/**
 * Dashboard search entry point (ROADMAP Phase 3: "Search entry"),
 * backed by SQLite FTS5 since Phase 4. Filters, tags, and the timeline
 * browse live in the full Search screen (`com.opfis.app.search`).
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
