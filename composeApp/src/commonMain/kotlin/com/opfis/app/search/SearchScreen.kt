package com.opfis.app.search

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.opfis.domain.search.SearchFilter
import com.opfis.domain.search.usecase.SearchFinancialRecordsUseCase
import com.opfis.domain.tag.usecase.AssignTagToTransactionUseCase
import com.opfis.domain.tag.usecase.DeleteTagUseCase
import com.opfis.domain.tag.usecase.ObserveTagsUseCase
import com.opfis.domain.tag.usecase.RemoveTagFromTransactionUseCase
import com.opfis.domain.tag.usecase.UpsertTagUseCase
import com.opfis.domain.timeline.usecase.ObserveTimelineUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * Global search (ROADMAP Phase 4): FTS5-backed text search with
 * entity-type and tag filters, falling back to a chronological,
 * taggable timeline browse when the query is blank. State and use-case
 * wiring live here; [SearchScreenBody] renders the actual layout.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun SearchScreen() {
    val searchFinancialRecords = koinInject<SearchFinancialRecordsUseCase>()
    val observeTimeline = koinInject<ObserveTimelineUseCase>()
    val observeTags = koinInject<ObserveTagsUseCase>()
    val upsertTag = koinInject<UpsertTagUseCase>()
    val deleteTag = koinInject<DeleteTagUseCase>()
    val assignTag = koinInject<AssignTagToTransactionUseCase>()
    val removeTag = koinInject<RemoveTagFromTransactionUseCase>()
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    val queryFlow = remember { MutableStateFlow("") }
    LaunchedEffect(query) { queryFlow.value = query }

    var filter by remember { mutableStateOf(SearchFilter.All) }
    val filterFlow = remember { MutableStateFlow(SearchFilter.All) }
    LaunchedEffect(filter) { filterFlow.value = filter }

    val tags by remember { observeTags() }.collectAsState(initial = emptyList())
    val searchResults by
        remember { searchFinancialRecords(queryFlow, filterFlow) }.collectAsState(initial = emptyList())
    val timeline by remember { observeTimeline(filterFlow) }.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Search") }) }) { padding ->
        SearchScreenBody(
            padding = padding,
            state = SearchScreenState(query, filter, tags, searchResults, timeline),
            actions =
                SearchScreenActions(
                    onQueryChange = { query = it },
                    onFilterChange = { filter = it },
                    onAssignTag = { transactionId, tagId -> scope.launch { assignTag(transactionId, tagId) } },
                    onRemoveTag = { transactionId, tagId -> scope.launch { removeTag(transactionId, tagId) } },
                    onCreateTag = { tag -> scope.launch { upsertTag(tag) } },
                    onDeleteTag = { tagId -> scope.launch { deleteTag(tagId) } },
                ),
        )
    }
}
