package com.opfis.app.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.SearchResult
import com.opfis.domain.tag.Tag
import com.opfis.domain.timeline.TimelineEntry
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Renders [SearchScreen]'s layout given already-collected state and callbacks. */
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
internal fun SearchScreenBody(
    padding: PaddingValues,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    tags: List<Tag>,
    searchResults: List<SearchResult>,
    timeline: List<TimelineEntry>,
    onAssignTag: (transactionId: String, tagId: String) -> Unit,
    onRemoveTag: (transactionId: String, tagId: String) -> Unit,
    onCreateTag: (Tag) -> Unit,
    onDeleteTag: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(16.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search accounts, categories, transactions, tags") },
            singleLine = true,
        )
        SearchFilterBar(filter = filter, onFilterChange = onFilterChange)
        TagFilterRow(
            tags = tags,
            selectedTagIds = filter.tagIds,
            onToggleTag = { tagId ->
                val updated = if (tagId in filter.tagIds) filter.tagIds - tagId else filter.tagIds + tagId
                onFilterChange(filter.copy(tagIds = updated))
            },
        )

        if (query.isNotBlank()) {
            GlobalSearchResultsList(query = query, results = searchResults)
        } else {
            TimelineSection(entries = timeline, tags = tags, onAssignTag = onAssignTag, onRemoveTag = onRemoveTag)
        }

        TagManagementSection(
            tags = tags,
            onCreateTag = { name ->
                val now = Clock.System.now().toEpochMilliseconds()
                onCreateTag(Tag(id = Uuid.random().toString(), name = name, createdAt = now, updatedAt = now))
            },
            onDeleteTag = onDeleteTag,
        )
    }
}
