package com.ar9988.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 시작 태그가 쓸모 있으려면 여기서 쓰레기를 걸러내야 한다.
 * "img" 나 "20240612" 가 태그로 제안되면 기능 전체가 우스워진다.
 */
class FileNameKeywordsTest {

    @Test
    fun `의미 있는 낱말을 뽑는다`() {
        val words = FileNameKeywords.extract("2024_출장_영수증.pdf")
        assertEquals(listOf("출장", "영수증"), words)
    }

    @Test
    fun `카메라와 스크린샷 이름은 아무것도 남기지 않는다`() {
        // 이런 파일에 붙일 이름은 파일명 안에 없다. 빈 목록이 맞다.
        assertTrue(FileNameKeywords.extract("IMG_0412.jpg").isEmpty())
        assertTrue(FileNameKeywords.extract("Screenshot_20240612_101530.png").isEmpty())
        assertTrue(FileNameKeywords.extract("PXL_20240101_120000000.jpg").isEmpty())
    }

    @Test
    fun `버전 꼬리표와 복사본 표시는 걸러진다`() {
        val words = FileNameKeywords.extract("계약서_final_v2_복사본.docx")
        assertEquals(listOf("계약서"), words)
    }

    @Test
    fun `식별자처럼 보이는 토큰은 걸러진다`() {
        val words = FileNameKeywords.extract("29dc03f1-9d36-415b-보고서.pdf")
        assertEquals(listOf("보고서"), words)
    }

    @Test
    fun `연도와 월은 이름이 아니라 시각이므로 뺀다`() {
        val words = FileNameKeywords.extract("2023년 12월 관리비.pdf")
        assertTrue("관리비" in words)
        assertTrue(words.none { it.any(Char::isDigit) })
    }

    @Test
    fun `너무 긴 토큰은 태그가 되기에 적당하지 않다`() {
        val words = FileNameKeywords.extract("supercalifragilisticexpialidocious_메모.txt")
        assertEquals(listOf("메모"), words)
    }

    @Test
    fun `대소문자가 달라도 같은 낱말로 모인다`() {
        assertEquals(
            FileNameKeywords.extract("Receipt_march.pdf"),
            FileNameKeywords.extract("RECEIPT_march.pdf")
        )
    }
}
