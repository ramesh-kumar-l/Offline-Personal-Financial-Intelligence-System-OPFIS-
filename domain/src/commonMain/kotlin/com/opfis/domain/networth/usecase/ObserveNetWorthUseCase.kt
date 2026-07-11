package com.opfis.domain.networth.usecase

import com.opfis.domain.account.AccountRepository
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.networth.NetWorthCalculator
import com.opfis.domain.networth.NetWorthSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Application-layer use case: recomputes [NetWorthSummary] whenever any
 * of the three underlying repositories change.
 */
class ObserveNetWorthUseCase(
    private val accountRepository: AccountRepository,
    private val assetRepository: AssetRepository,
    private val liabilityRepository: LiabilityRepository,
) {
    operator fun invoke(): Flow<NetWorthSummary> =
        combine(
            accountRepository.observeAll(),
            assetRepository.observeAll(),
            liabilityRepository.observeAll(),
        ) { accounts, assets, liabilities ->
            NetWorthCalculator.calculate(accounts, assets, liabilities)
        }
}
