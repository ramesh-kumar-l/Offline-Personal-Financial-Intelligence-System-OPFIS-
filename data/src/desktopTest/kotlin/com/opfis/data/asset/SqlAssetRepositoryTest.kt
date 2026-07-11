package com.opfis.data.asset

import com.opfis.data.testDatabase
import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlAssetRepositoryTest {
    @Test
    fun `upsert persists an asset valuation`() =
        runTest {
            val repository = SqlAssetRepository(testDatabase())
            val asset =
                Asset(
                    id = "asset-1",
                    name = "Gold ETF",
                    type = AssetType.GOLD,
                    valueMinorUnits = 250_000L,
                    createdAt = 0L,
                    updatedAt = 0L,
                )

            repository.upsert(asset)
            repository.upsert(asset.copy(valueMinorUnits = 260_000L))
            val assets = repository.observeAll().first()

            assertEquals(1, assets.size)
            assertEquals(260_000L, assets.single().valueMinorUnits)
        }
}
