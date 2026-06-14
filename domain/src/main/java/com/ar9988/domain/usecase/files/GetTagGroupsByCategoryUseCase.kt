package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagGroupsByCategoryUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository,
) {
    operator fun invoke(category: FileCategory): Flow<List<CategoryTagGroupModel>> {
        return resourceRepository.getTagGroupsByCategory(category)
    }
}