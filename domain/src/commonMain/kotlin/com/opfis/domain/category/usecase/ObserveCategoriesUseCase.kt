package com.opfis.domain.category.usecase

import com.opfis.domain.category.Category
import com.opfis.domain.category.CategoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveCategoriesUseCase(
    private val repository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = repository.observeAll()
}
