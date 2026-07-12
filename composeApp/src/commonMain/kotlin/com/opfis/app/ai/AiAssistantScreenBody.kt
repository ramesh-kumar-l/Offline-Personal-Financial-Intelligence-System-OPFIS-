package com.opfis.app.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Renders [AiAssistantScreen]'s layout: a question input row above the conversation history. */
@Composable
internal fun AiAssistantScreenBody(
    padding: PaddingValues,
    exchanges: List<AiExchange>,
    isLoading: Boolean,
    onAsk: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AskQuestionForm(isLoading = isLoading, onAsk = onAsk)

        if (exchanges.isEmpty()) {
            Text(
                "Ask about your net worth, spending, cash flow, budgets, or goals.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            exchanges.forEach { exchange -> AiExchangeCard(exchange) }
        }
    }
}

@Composable
private fun AskQuestionForm(
    isLoading: Boolean,
    onAsk: (String) -> Unit,
) {
    var question by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ask a question", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Question") },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        val trimmed = question.trim()
                        if (trimmed.isNotEmpty()) {
                            onAsk(trimmed)
                            question = ""
                        }
                    },
                ) {
                    Text(if (isLoading) "Thinking..." else "Ask")
                }
            }
        }
    }
}

@Composable
private fun AiExchangeCard(exchange: AiExchange) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(exchange.question, style = MaterialTheme.typography.titleSmall)
            Text(exchange.answer.text, style = MaterialTheme.typography.bodyMedium)
            if (exchange.answer.citations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    exchange.answer.citations.forEach { citation -> AiCitationRow(citation) }
                }
            }
        }
    }
}
