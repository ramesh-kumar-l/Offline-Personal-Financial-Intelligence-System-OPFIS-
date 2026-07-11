package com.opfis.domain.account.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountRepository
import kotlinx.coroutines.flow.Flow

class ObserveAccountsUseCase(
    private val repository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = repository.observeAll()
}
