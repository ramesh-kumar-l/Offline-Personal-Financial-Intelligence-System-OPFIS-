package com.opfis.domain.asset.usecase

import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.asset.AssetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeAssetRepository(
    private val assets: List<Asset> = emptyList(),
) : AssetRepository {
    val upserted = mutableListOf<Asset>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<Asset>> = flowOf(assets)

    override suspend fun upsert(asset: Asset) {
        upserted.add(asset)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

class AssetUseCasesTest {
    private val asset = Asset("asset-1", "House", AssetType.REAL_ESTATE, 500_000L, createdAt = 0L, updatedAt = 0L)

    @Test
    fun `observe assets returns the repository stream`() =
        runTest {
            val useCase = ObserveAssetsUseCase(FakeAssetRepository(listOf(asset)))
            assertEquals(listOf(asset), useCase().first())
        }

    @Test
    fun `upsert asset delegates to the repository`() =
        runTest {
            val repository = FakeAssetRepository()
            UpsertAssetUseCase(repository)(asset)
            assertEquals(listOf(asset), repository.upserted)
        }

    @Test
    fun `delete asset delegates to the repository`() =
        runTest {
            val repository = FakeAssetRepository()
            DeleteAssetUseCase(repository)(asset.id)
            assertEquals(listOf(asset.id), repository.deleted)
        }
}
