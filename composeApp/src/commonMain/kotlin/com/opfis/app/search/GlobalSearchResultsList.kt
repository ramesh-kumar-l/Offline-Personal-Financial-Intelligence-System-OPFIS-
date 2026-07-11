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
import com.opfis.domain.search.SearchResult

/** FTS5-ranked global search results (ROADMAP Phase 4). */
@Composable
fun GlobalSearchResultsList(
    query: String,
    results: List<SearchResult>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Results", style = MaterialTheme.typography.titleMedium)
            if (results.isEmpty()) {
                Text("No results for \"$query\"", style = MaterialTheme.typography.bodySmall)
            } else {
                results.forEach { result -> SearchResultRow(result) }
            }
        }
    }
}
