package com.opfis.data.budget

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetPeriod
import com.opfis.domain.budget.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.opfis.data.db.Budget as BudgetRow

class SqlBudgetRepository(
    private val database: OpfisDatabase,
) : BudgetRepository {
    override fun observeAll(): Flow<List<Budget>> =
        database.budgetQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomain) }

    override suspend fun upsert(budget: Budget) {
        val existingVersion =
            database.budgetQueries
                .selectById(budget.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.budgetQueries.insertOrReplace(
            id = budget.id,
            category_id = budget.categoryId,
            limit_minor_units = budget.limitMinorUnits,
            period = budget.period.name,
            start_date = budget.startDate,
            created_at = budget.createdAt,
            updated_at = budget.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.budgetQueries.deleteById(id)
    }

    private fun toDomain(row: BudgetRow): Budget =
        Budget(
            id = row.id,
            categoryId = row.category_id,
            limitMinorUnits = row.limit_minor_units,
            period = BudgetPeriod.valueOf(row.period),
            startDate = row.start_date,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
        )
}
