package com.example.domain.usecase.tag

import com.example.domain.repository.TagRepository
import javax.inject.Inject

class UpdateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
){
    operator fun invoke(tagId: Long, tagName: String, tagColor: Long): Result<Unit> {
        return runCatching {
            val normalizedName = tagName.trim()

            require(normalizedName.length >= 2) {
                "태그 이름은 최소 2자 이상이어야 합니다."
            }

            tagRepository.updateTag(tagId, normalizedName, tagColor)
        }
    }
}