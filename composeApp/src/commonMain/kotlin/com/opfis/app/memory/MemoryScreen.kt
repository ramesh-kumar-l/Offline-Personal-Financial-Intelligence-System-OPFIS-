package com.opfis.app.memory

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventType
import com.opfis.domain.memory.usecase.DeleteMemoryEventUseCase
import com.opfis.domain.memory.usecase.ObserveMemoryTimelineUseCase
import com.opfis.domain.memory.usecase.RecordMemoryEventUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Financial memory timeline (ROADMAP Phase 6): a persistent, searchable
 * journal of notes and milestones the user records - see
 * `13-memory-engine.md`. State/wiring live here;
 * [MemoryScreenBody] renders the layout.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun MemoryScreen() {
    val observeMemoryTimeline = koinInject<ObserveMemoryTimelineUseCase>()
    val recordMemoryEvent = koinInject<RecordMemoryEventUseCase>()
    val deleteMemoryEvent = koinInject<DeleteMemoryEventUseCase>()
    val scope = rememberCoroutineScope()

    val memoryEvents by remember { observeMemoryTimeline() }.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Financial Memory") }) }) { padding ->
        MemoryScreenBody(
            padding = padding,
            memoryEvents = memoryEvents,
            onRecordEvent = { title, description, eventType ->
                scope.launch {
                    val now = Clock.System.now().toEpochMilliseconds()
                    recordMemoryEvent(
                        MemoryEvent(
                            id = Uuid.random().toString(),
                            eventType = eventType,
                            title = title,
                            description = description,
                            subject = null,
                            occurredAt = now,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )
                }
            },
            onDeleteEvent = { id -> scope.launch { deleteMemoryEvent(id) } },
        )
    }
}

internal typealias RecordEventCallback = (title: String, description: String, eventType: MemoryEventType) -> Unit
