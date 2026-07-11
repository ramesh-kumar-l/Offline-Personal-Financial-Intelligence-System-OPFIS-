package com.opfis.domain.systemstatus

import kotlinx.coroutines.flow.Flow

/**
 * Domain-owned port for observing [SystemStatus]. Implemented by the
 * Infrastructure layer (`:data`); the Domain layer never knows how the
 * status is produced.
 */
interface SystemStatusRepository {
    fun observe(): Flow<SystemStatus>
}
