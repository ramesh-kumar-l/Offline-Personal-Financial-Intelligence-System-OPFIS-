package com.opfis.data.systemstatus

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.systemstatus.SystemStatus
import com.opfis.domain.systemstatus.SystemStatusRepository
import com.opfis.domain.systemstatus.TrustIndicator
import com.opfis.domain.systemstatus.TrustIndicatorState
import com.opfis.shared.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Phase 1 implementation of [SystemStatusRepository], backed by the
 * encrypted SQLDelight/SQLCipher database (docs/adr/0005). Unlike
 * Phase 0's static in-memory version, `encrypted_storage` now reports
 * ACTIVE because it observes a real encrypted database, not a
 * placeholder.
 */
@OptIn(ExperimentalTime::class)
class PersistentSystemStatusRepository(
    private val database: OpfisDatabase,
    private val logger: Logger,
) : SystemStatusRepository {
    init {
        seedDefaultIndicatorsIfEmpty()
    }

    override fun observe(): Flow<SystemStatus> =
        database.systemStatusQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> SystemStatus(indicators = rows.map(::toDomain)) }

    private fun seedDefaultIndicatorsIfEmpty() {
        val existing = database.systemStatusQueries.selectAll().executeAsList()
        if (existing.isNotEmpty()) return

        logger.debug(TAG, "Seeding default trust indicators")
        val now = Clock.System.now().toEpochMilliseconds()
        DEFAULT_INDICATORS.forEach { seed ->
            database.systemStatusQueries.insertOrReplace(
                id = seed.id,
                label = seed.label,
                state = seed.state.name,
                created_at = now,
                updated_at = now,
                version = 1,
            )
        }
    }

    private fun toDomain(row: com.opfis.data.db.System_status_indicator): TrustIndicator =
        TrustIndicator(
            id = row.id,
            label = row.label,
            state = TrustIndicatorState.valueOf(row.state),
        )

    private data class SeedIndicator(
        val id: String,
        val label: String,
        val state: TrustIndicatorState,
    )

    private companion object {
        const val TAG = "PersistentSystemStatusRepository"
        val DEFAULT_INDICATORS =
            listOf(
                SeedIndicator("offline_mode", "Offline Mode", TrustIndicatorState.ACTIVE),
                SeedIndicator("no_cloud_connected", "No Cloud Connected", TrustIndicatorState.ACTIVE),
                SeedIndicator("encrypted_storage", "Encrypted Storage", TrustIndicatorState.ACTIVE),
                SeedIndicator("local_ai", "Local AI", TrustIndicatorState.PENDING),
            )
    }
}
