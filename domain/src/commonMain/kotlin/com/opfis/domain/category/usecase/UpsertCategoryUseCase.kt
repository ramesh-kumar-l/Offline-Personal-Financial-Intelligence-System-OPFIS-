package com.opfis.domain.category.usecase

import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryRepository

class UpsertCategoryUseCase(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(category: Category) = repository.upsert(category)
}
