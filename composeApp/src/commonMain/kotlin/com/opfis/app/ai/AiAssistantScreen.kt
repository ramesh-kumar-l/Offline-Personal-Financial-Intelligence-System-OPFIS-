package com.opfis.app.ai

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.opfis.domain.ai.AiAnswer
import com.opfis.domain.ai.usecase.AskAiAssistantUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** One question/answer exchange shown in [AiAssistantScreenBody]'s conversation list. */
internal data class AiExchange(
    val question: String,
    val answer: AiAnswer,
)

/**
 * Offline AI assistant (ROADMAP Phase 7): asks [AskAiAssistantUseCase],
 * which answers deterministically from the user's own data - see
 * `15-ai-runtime.md`. State/wiring live here; [AiAssistantScreenBody]
 * renders the layout.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AiAssistantScreen() {
    val askAiAssistant = koinInject<AskAiAssistantUseCase>()
    val scope = rememberCoroutineScope()

    val exchanges = remember { mutableStateListOf<AiExchange>() }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("AI Assistant") }) }) { padding ->
        AiAssistantScreenBody(
            padding = padding,
            exchanges = exchanges,
            isLoading = isLoading,
            onAsk = { question ->
                isLoading = true
                scope.launch {
                    val now = Clock.System.now().toEpochMilliseconds()
                    val answer = askAiAssistant(question, now)
                    exchanges.add(0, AiExchange(question, answer))
                    isLoading = false
                }
            },
        )
    }
}
