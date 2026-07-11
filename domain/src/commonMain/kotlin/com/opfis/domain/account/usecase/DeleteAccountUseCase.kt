package com.opfis.domain.account.usecase

import com.opfis.domain.account.AccountRepository

class DeleteAccountUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
