package com.opfis.app.systemstatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.systemstatus.SystemStatus
import com.opfis.domain.systemstatus.TrustIndicator
import com.opfis.domain.systemstatus.TrustIndicatorState
import com.opfis.domain.systemstatus.usecase.ObserveSystemStatusUseCase
import org.koin.compose.koinInject

/**
 * The first real screen of OPFIS: a list of trust indicators
 * (SystemPrompt Part 3, "Trust Indicators" and "Home Dashboard").
 * Later phases replace this with the full dashboard; this screen
 * exists in Phase 0 to prove the Presentation -> Application ->
 * Domain -> Infrastructure round trip compiles, wires through Koin,
 * and renders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemStatusScreen() {
    val observeSystemStatus = koinInject<ObserveSystemStatusUseCase>()
    val status by remember { observeSystemStatus() }
        .collectAsState(initial = SystemStatus(indicators = emptyList()))

    Scaffold(
        topBar = { TopAppBar(title = { Text("OPFIS") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(status.indicators) { indicator ->
                TrustIndicatorRow(indicator)
            }
        }
    }
}

@Composable
private fun TrustIndicatorRow(indicator: TrustIndicator) {
    val (marker, tint) =
        when (indicator.state) {
            TrustIndicatorState.ACTIVE -> "●" to OpfisColors.Success
            TrustIndicatorState.PENDING -> "●" to OpfisColors.Warning
        }
    val stateLabel =
        when (indicator.state) {
            TrustIndicatorState.ACTIVE -> "Active"
            TrustIndicatorState.PENDING -> "Pending"
        }

    Column {
        Text(text = "$marker ${indicator.label}", color = tint, style = MaterialTheme.typography.bodyLarge)
        Text(text = stateLabel, style = MaterialTheme.typography.bodySmall)
    }
}
