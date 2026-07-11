package com.opfis.domain.tag.usecase

import com.opfis.domain.tag.Tag
import com.opfis.domain.tag.TagRepository

class UpsertTagUseCase(
    private val repository: TagRepository,
) {
    suspend operator fun invoke(tag: Tag) = repository.upsert(tag)
}
