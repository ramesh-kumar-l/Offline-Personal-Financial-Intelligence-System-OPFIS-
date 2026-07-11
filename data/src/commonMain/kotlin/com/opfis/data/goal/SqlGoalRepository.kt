package com.opfis.data.goal

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.goal.Goal
import com.opfis.domain.goal.GoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.opfis.data.db.Goal as GoalRow

class SqlGoalRepository(
    private val database: OpfisDatabase,
) : GoalRepository {
    override fun observeAll(): Flow<List<Goal>> =
        database.goalQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomain) }

    override suspend fun upsert(goal: Goal) {
        val existingVersion =
            database.goalQueries
                .selectById(goal.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.goalQueries.insertOrReplace(
            id = goal.id,
            name = goal.name,
            target_amount_minor_units = goal.targetAmountMinorUnits,
            current_amount_minor_units = goal.currentAmountMinorUnits,
            target_date = goal.targetDate,
            created_at = goal.createdAt,
            updated_at = goal.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.goalQueries.deleteById(id)
    }

    private fun toDomain(row: GoalRow): Goal =
        Goal(
            id = row.id,
            name = row.name,
            targetAmountMinorUnits = row.target_amount_minor_units,
            currentAmountMinorUnits = row.current_amount_minor_units,
            targetDate = row.target_date,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
        )
}
