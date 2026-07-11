package com.opfis.domain.search.usecase

import com.opfis.domain.account.AccountRepository
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.search.FinancialSearchEngine
import com.opfis.domain.search.SearchResult
import com.opfis.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Application-layer use case behind the dashboard's search entry point.
 * Recomputes matches whenever the query or any searched repository
 * changes.
 */
class SearchFinancialRecordsUseCase(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(query: Flow<String>): Flow<List<SearchResult>> =
        combine(
            accountRepository.observeAll(),
            categoryRepository.observeAll(),
            transactionRepository.observeAll(),
            query,
        ) { accounts, categories, transactions, currentQuery ->
            FinancialSearchEngine.search(currentQuery, accounts, categories, transactions)
        }
}
