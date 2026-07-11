package com.opfis.domain.category.usecase

import com.opfis.domain.category.CategoryRepository

class DeleteCategoryUseCase(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
