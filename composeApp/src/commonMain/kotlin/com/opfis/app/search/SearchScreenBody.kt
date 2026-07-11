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

/** [SearchScreen]'s already-collected state, grouped to keep [SearchScreenBody]'s parameter count in check. */
internal data class SearchScreenState(
    val query: String,
    val filter: SearchFilter,
    val tags: List<Tag>,
    val searchResults: List<SearchResult>,
    val timeline: List<TimelineEntry>,
)

/** [SearchScreen]'s callbacks, grouped to keep [SearchScreenBody]'s parameter count in check. */
internal data class SearchScreenActions(
    val onQueryChange: (String) -> Unit,
    val onFilterChange: (SearchFilter) -> Unit,
    val onAssignTag: (transactionId: String, tagId: String) -> Unit,
    val onRemoveTag: (transactionId: String, tagId: String) -> Unit,
    val onCreateTag: (Tag) -> Unit,
    val onDeleteTag: (String) -> Unit,
)

/** Renders [SearchScreen]'s layout given already-collected state and callbacks. */
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
internal fun SearchScreenBody(
    padding: PaddingValues,
    state: SearchScreenState,
    actions: SearchScreenActions,
) {
    val (query, filter, tags, searchResults, timeline) = state
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
            onValueChange = actions.onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search accounts, categories, transactions, tags") },
            singleLine = true,
        )
        SearchFilterBar(filter = filter, onFilterChange = actions.onFilterChange)
        TagFilterRow(
            tags = tags,
            selectedTagIds = filter.tagIds,
            onToggleTag = { tagId ->
                val updated = if (tagId in filter.tagIds) filter.tagIds - tagId else filter.tagIds + tagId
                actions.onFilterChange(filter.copy(tagIds = updated))
            },
        )

        if (query.isNotBlank()) {
            GlobalSearchResultsList(query = query, results = searchResults)
        } else {
            TimelineSection(
                entries = timeline,
                tags = tags,
                onAssignTag = actions.onAssignTag,
                onRemoveTag = actions.onRemoveTag,
            )
        }

        TagManagementSection(
            tags = tags,
            onCreateTag = { name ->
                val now = Clock.System.now().toEpochMilliseconds()
                actions.onCreateTag(Tag(id = Uuid.random().toString(), name = name, createdAt = now, updatedAt = now))
            },
            onDeleteTag = actions.onDeleteTag,
        )
    }
}
