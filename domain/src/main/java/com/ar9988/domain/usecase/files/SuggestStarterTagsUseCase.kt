package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.StarterTagSuggestion
import com.ar9988.domain.repository.ResourceRepository
import com.ar9988.domain.service.EmbeddingModel
import com.ar9988.domain.util.FileNameKeywords
import com.ar9988.domain.util.cosineSimilarity
import javax.inject.Inject

/**
 * 첫 색인이 끝났을 때 시작 태그를 제안한다.
 *
 * 태그 기반 관리에는 부트스트랩 문제가 있다 — 태그를 붙이기 전까지는 아무 가치가 없는데,
 * 태그를 붙이는 건 일이다. 그래서 처음 한 벌은 앱이 만들어 준다.
 *
 * 비용 때문에 파일 **내용은 읽지 않는다.** 이름은 색인할 때 이미 DB 에 들어와 있으므로
 * 전체 파일을 훑어도 I/O 가 없다. 내용까지 읽으면(특히 이미지 OCR) 첫 실행이 몇 분씩
 * 걸려서, 첫인상을 좋게 하려던 기능이 정반대가 된다.
 *
 * 임베딩은 발견이 아니라 **정리**에만 쓴다. 후보 낱말 몇십 개만 인코딩해서
 * "영수증 / 영수증들 / receipt" 처럼 사실상 같은 것을 하나로 합친다.
 */
class SuggestStarterTagsUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository,
    private val embeddingModel: EmbeddingModel,
) {

    suspend operator fun invoke(): List<StarterTagSuggestion> {
        val files = resourceRepository.getAllFileNames()
        if (files.size < MIN_LIBRARY_SIZE) return emptyList()

        // 1) 낱말 → 그 낱말이 들어간 파일들
        val wordToFiles = mutableMapOf<String, MutableList<Long>>()
        files.forEach { file ->
            FileNameKeywords.extract(file.name).forEach { word ->
                wordToFiles.getOrPut(word) { mutableListOf() }.add(file.id)
            }
        }

        // 2) 너무 드문 낱말은 태그가 될 만큼 되풀이되지 않은 것이다.
        //    너무 흔한 낱말은 거의 모든 파일에 붙어서 아무것도 구분해 주지 못한다.
        val tooCommon = (files.size * MAX_COVERAGE).toInt().coerceAtLeast(MIN_FILES_PER_TAG + 1)
        val candidates = wordToFiles
            .filter { (_, ids) -> ids.size in MIN_FILES_PER_TAG..tooCommon }
            .entries
            .sortedByDescending { it.value.size }
            .take(MAX_CANDIDATES)
            .map { StarterTagSuggestion(it.key, it.value.distinct()) }

        if (candidates.isEmpty()) return emptyList()

        return mergeSimilar(candidates).take(MAX_SUGGESTIONS)
    }

    /**
     * 뜻이 겹치는 후보를 합친다.
     *
     * 파일 수가 많은 쪽을 대표로 남기고, 비슷한 것들의 파일 목록을 그쪽으로 흡수시킨다.
     * 후보가 [MAX_CANDIDATES] 개뿐이라 인코딩 횟수도 그만큼이다.
     */
    private suspend fun mergeSimilar(
        candidates: List<StarterTagSuggestion>
    ): List<StarterTagSuggestion> {
        val embeddings = candidates.associate { it.name to embeddingModel.encode(it.name) }

        val merged = mutableListOf<StarterTagSuggestion>()
        val absorbed = mutableSetOf<String>()

        // 파일이 많은 순으로 이미 정렬돼 있으므로, 앞에서부터 대표가 된다.
        candidates.forEach { candidate ->
            if (candidate.name in absorbed) return@forEach

            val fileIds = candidate.fileIds.toMutableSet()

            candidates.forEach { other ->
                if (other.name == candidate.name || other.name in absorbed) return@forEach

                val similarity = cosineSimilarity(
                    embeddings.getValue(candidate.name),
                    embeddings.getValue(other.name)
                )
                if (similarity >= MERGE_THRESHOLD) {
                    absorbed += other.name
                    fileIds += other.fileIds
                }
            }

            merged += candidate.copy(fileIds = fileIds.toList())
        }

        return merged.sortedByDescending { it.fileCount }
    }

    private companion object {
        /** 파일이 이만큼도 없으면 되풀이되는 주제를 찾을 수 없다. */
        const val MIN_LIBRARY_SIZE = 20

        /** 이보다 적게 나오는 낱말은 태그가 아니라 그냥 그 파일의 이름이다. */
        const val MIN_FILES_PER_TAG = 3

        /** 전체의 이 비율을 넘게 덮는 낱말은 구분에 쓸모가 없다. */
        const val MAX_COVERAGE = 0.4f

        const val MAX_CANDIDATES = 30
        const val MAX_SUGGESTIONS = 6

        /**
         * 후보를 합칠 유사도 기준.
         *
         * 검색용 임계값(0.42~0.6)보다 높게 잡는다. 검색은 느슨하게 걸어도 사용자가
         * 결과를 보고 판단하지만, 여기서는 잘못 합치면 서로 다른 두 주제가
         * 한 태그로 뭉개진 채 그대로 만들어진다.
         */
        const val MERGE_THRESHOLD = 0.78f
    }
}
