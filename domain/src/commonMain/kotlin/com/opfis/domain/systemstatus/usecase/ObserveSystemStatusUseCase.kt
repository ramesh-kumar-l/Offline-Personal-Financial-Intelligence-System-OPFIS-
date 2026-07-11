package com.opfis.domain.systemstatus.usecase

import com.opfis.domain.systemstatus.SystemStatus
import com.opfis.domain.systemstatus.SystemStatusRepository
import kotlinx.coroutines.flow.Flow

/**
 * Application-layer use case: exposes the current [SystemStatus] to the
 * Presentation layer. Lives inside the `:domain` module (see ADR 0001)
 * but is conceptually the Application layer - it coordinates a
 * repository call and applies no business rules of its own yet.
 */
class ObserveSystemStatusUseCase(
    private val repository: SystemStatusRepository,
) {
    operator fun invoke(): Flow<SystemStatus> = repository.observe()
}
