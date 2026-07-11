package com.opfis.domain.tag.usecase

import com.opfis.domain.tag.Tag
import com.opfis.domain.tag.TagRepository
import kotlinx.coroutines.flow.Flow

class ObserveTagsUseCase(
    private val repository: TagRepository,
) {
    operator fun invoke(): Flow<List<Tag>> = repository.observeAll()
}
