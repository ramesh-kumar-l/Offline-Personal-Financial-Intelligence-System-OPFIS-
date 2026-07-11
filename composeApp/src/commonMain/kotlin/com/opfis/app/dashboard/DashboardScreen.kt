package com.opfis.app.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.domain.cashflow.usecase.ObserveCashFlowUseCase
import com.opfis.domain.networth.usecase.ObserveNetWorthUseCase
import com.opfis.domain.search.usecase.SearchFinancialRecordsUseCase
import com.opfis.domain.systemstatus.SystemStatus
import com.opfis.domain.systemstatus.usecase.ObserveSystemStatusUseCase
import com.opfis.domain.transaction.usecase.ObserveRecentTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject

/**
 * The app's home screen (SystemPrompt Part 3, "Home Dashboard";
 * ROADMAP Phase 3). Answers "what is my current financial health?"
 * with Net Worth, Cash Flow, Recent Activity, and a search entry point,
 * plus the Phase 0 trust-indicator strip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val observeSystemStatus = koinInject<ObserveSystemStatusUseCase>()
    val observeNetWorth = koinInject<ObserveNetWorthUseCase>()
    val observeCashFlow = koinInject<ObserveCashFlowUseCase>()
    val observeRecentTransactions = koinInject<ObserveRecentTransactionsUseCase>()
    val searchFinancialRecords = koinInject<SearchFinancialRecordsUseCase>()

    val status by remember { observeSystemStatus() }.collectAsState(initial = SystemStatus(indicators = emptyList()))
    val netWorth by remember { observeNetWorth() }.collectAsState(initial = null)
    val cashFlowPeriods by remember { observeCashFlow() }.collectAsState(initial = emptyList())
    val recentTransactions by remember { observeRecentTransactions() }.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    val searchQueryFlow = remember { MutableStateFlow("") }
    LaunchedEffect(searchQuery) { searchQueryFlow.value = searchQuery }
    val searchResults by remember { searchFinancialRecords(searchQueryFlow) }.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("OPFIS") }) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(PaddingValues(16.dp)),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TrustIndicatorsSection(status.indicators)
            SearchBarSection(query = searchQuery, onQueryChange = { searchQuery = it }, results = searchResults)
            netWorth?.let { summary -> NetWorthSection(summary) }
            CashFlowSection(cashFlowPeriods)
            RecentActivitySection(recentTransactions)
        }
    }
}
