package com.opfis.data.liability

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.liability.LiabilityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.opfis.data.db.Liability as LiabilityRow

class SqlLiabilityRepository(
    private val database: OpfisDatabase,
) : LiabilityRepository {
    override fun observeAll(): Flow<List<Liability>> =
        database.liabilityQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomain) }

    override suspend fun upsert(liability: Liability) {
        val existingVersion =
            database.liabilityQueries
                .selectById(liability.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.liabilityQueries.insertOrReplace(
            id = liability.id,
            name = liability.name,
            type = liability.type.name,
            balance_minor_units = liability.balanceMinorUnits,
            interest_rate_basis_points = liability.interestRateBasisPoints?.toLong(),
            created_at = liability.createdAt,
            updated_at = liability.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.liabilityQueries.deleteById(id)
    }

    private fun toDomain(row: LiabilityRow): Liability =
        Liability(
            id = row.id,
            name = row.name,
            type = LiabilityType.valueOf(row.type),
            balanceMinorUnits = row.balance_minor_units,
            interestRateBasisPoints = row.interest_rate_basis_points?.toInt(),
            createdAt = row.created_at,
            updatedAt = row.updated_at,
        )
}
