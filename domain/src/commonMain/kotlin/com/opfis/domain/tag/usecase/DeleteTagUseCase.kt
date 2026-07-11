package com.opfis.domain.tag.usecase

import com.opfis.domain.tag.TagRepository

class DeleteTagUseCase(
    private val repository: TagRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
