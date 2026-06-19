package com.ar9988.tagfilemanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ar9988.local_db.processor.OnnxEmbeddingModel
import junit.framework.TestCase.fail
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.sqrt

@RunWith(AndroidJUnit4::class)
class EmbeddingSimilarityTest {

    private val context =
        ApplicationProvider.getApplicationContext<Context>()

    private val model =
        OnnxEmbeddingModel(context)


    val testSets = listOf(
        SimilarityCase("영수증", "결제", minSimilarity = 0.45f),
        SimilarityCase("스타벅스", "카페", minSimilarity = 0.45f),
        SimilarityCase("아메리카노", "커피", minSimilarity = 0.5f),

        // 업무 채널
        SimilarityCase("회의록", "업무", minSimilarity = 0.45f),
        SimilarityCase("이력서", "채용 공고", minSimilarity = 0.43f),
        SimilarityCase("보고서", "문서", minSimilarity = 0.5f),

        // 여행 채널
        SimilarityCase("항공권", "티켓", minSimilarity = 0.5f),
        SimilarityCase("호텔", "숙소", minSimilarity = 0.55f),
        SimilarityCase("일정표", "계획", minSimilarity = 0.43f),

        // 교육 채널
        SimilarityCase("수료증", "교육", minSimilarity = 0.45f),
        SimilarityCase("자격증", "증명서", minSimilarity = 0.5f)
    )

    @Test
    fun embedding_dilution_experiment() = runBlocking {
        // 기준점: 사용자가 입력할 검색어 (또는 순수 태그명)
        val query = model.encode("영수증")

        // Case 1: 정제된 텍스트 (파일명 + 핵심 키워드만 압축된 상태)
        val cleanText = "스타벅스 영수증 카페 결제 금액 내역"
        val embeddingCase1 = model.encode(cleanText)
        val simCase1 = cosineSimilarity(query, embeddingCase1)

        // Case 2: 중간 오염 (OCR로 추출된 영수증 내부의 온갖 잡다한 텍스트 포함)
        val muddyText =
            "스타벅스 영수증 카페 결제 금액 내역 주소 서울시 강남구 대표자 홍길동 사업자번호 123-45-67890 2026-05-16 부가세 면세 물품 아이스 아메리카노 수량 1"
        val embeddingCase2 = model.encode(muddyText)
        val simCase2 = cosineSimilarity(query, embeddingCase2)

        // Case 3: 심각한 오염 (텍스트 추출량이 너무 많아 하단에 다른 내용까지 다 긁어온 경우)
        val heavilyDilutedText =
            "스타벅스 영수증 카페 결제 금액 내역 주소 서울시 강남구 대표자 홍길동 사업자번호 123-45-67890 2026-05-16 부가세 면세 물품 아이스 아메리카노 수량 1 교환 환불은 7일 이내 영수증 지참 후 방문 바랍니다 무료 주차 등록은 파트너에게 말씀해주세요 와이파이 비밀번호 스벅1234 스타벅스 앱 카드를 이용하시면 별이 적립됩니다"
        val embeddingCase3 = model.encode(heavilyDilutedText)
        val simCase3 = cosineSimilarity(query, embeddingCase3)

        println("==================================================")
        println("📊 [임베딩 오염 테스트 결과]")
        println("1️⃣ 핵심만 정제된 텍스트 유사도 : $simCase1")
        println("2️⃣ 일반 OCR 텍스트 유사도     : $simCase2")
        println("3️⃣ 과도하게 긴 텍스트 유사도   : $simCase3")
        println("==================================================")

        // 🧐 엔지니어링 가이드라인을 위한 단언(Assertion)
        // 텍스트가 길어질수록 점수가 계단식으로 떨어지는지 확인합니다.
        assert(simCase1 > simCase2) { "오염이 진행되었으나 점수 차이가 크지 않습니다." }
        assert(simCase2 > simCase3) { "텍스트가 더 길어졌음에도 벡터가 유지되고 있습니다." }
    }

    @Test
    fun embedding_similarity_test() = runBlocking {

        val failures = mutableListOf<String>()

        testSets.forEach { test ->

            val embeddingA = model.encode(test.a)
            val embeddingB = model.encode(test.b)

            val similarity =
                cosineSimilarity(embeddingA, embeddingB)

            println(
                """
            ============================
            ${test.a} <-> ${test.b}
            similarity = $similarity
            ============================
            """.trimIndent()
            )

            test.minSimilarity?.let {
                if (similarity < it) {
                    failures +=
                        "[FAIL] ${test.a} <-> ${test.b} : " +
                                "$similarity < $it"
                }
            }

            test.maxSimilarity?.let {
                if (similarity > it) {
                    failures +=
                        "[FAIL] ${test.a} <-> ${test.b} : " +
                                "$similarity > $it"
                }
            }
        }

        if (failures.isNotEmpty()) {
            fail(
                failures.joinToString("\n")
            )
        }
    }

    private fun cosineSimilarity(
        a: FloatArray,
        b: FloatArray
    ): Float {

        var dot = 0f
        var normA = 0f
        var normB = 0f

        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        val denominator =
            sqrt(normA) * sqrt(normB)

        return if (denominator == 0f) {
            0f
        } else {
            dot / denominator
        }
    }

    data class SimilarityCase(
        val a: String,
        val b: String,
        val minSimilarity: Float? = null,
        val maxSimilarity: Float? = null
    )
}