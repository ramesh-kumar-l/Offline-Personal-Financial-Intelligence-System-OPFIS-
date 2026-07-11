package com.opfis.domain.account.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountRepository

class UpsertAccountUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(account: Account) = repository.upsert(account)
}
