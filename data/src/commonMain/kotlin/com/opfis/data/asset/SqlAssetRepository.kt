package com.opfis.data.asset

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.asset.AssetType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.opfis.data.db.Asset as AssetRow

class SqlAssetRepository(
    private val database: OpfisDatabase,
) : AssetRepository {
    override fun observeAll(): Flow<List<Asset>> =
        database.assetQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomain) }

    override suspend fun upsert(asset: Asset) {
        val existingVersion =
            database.assetQueries
                .selectById(asset.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.assetQueries.insertOrReplace(
            id = asset.id,
            name = asset.name,
            type = asset.type.name,
            value_minor_units = asset.valueMinorUnits,
            created_at = asset.createdAt,
            updated_at = asset.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.assetQueries.deleteById(id)
    }

    private fun toDomain(row: AssetRow): Asset =
        Asset(
            id = row.id,
            name = row.name,
            type = AssetType.valueOf(row.type),
            valueMinorUnits = row.value_minor_units,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
        )
}
