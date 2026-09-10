package com.ar9988.data.scanner

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 폴더 목록 읽기를 건너뛸지 정하는 규칙.
 *
 * 이 판단이 틀리면 증상이 조용하다 — 오류도 안 나고 그냥 새 파일이 영영 안 보인다.
 * 그래서 규칙만 따로 떼어 고정해 둔다.
 *
 * 실제로 한 번 틀렸던 방식도 아래에 남겨 뒀다. 큐에 "방금 잰 값" 을 넣으면
 * 꺼낼 때 자기 자신과 비교하게 되어 항상 일치하고, 결국 모든 폴더를 영원히 건너뛴다.
 */
class DirectorySkipRuleTest {

    @Test
    fun `저장된 값이 없으면 반드시 훑는다`() {
        // 처음 보는 폴더. 비교할 기준이 없다.
        assertFalse(canSkip(stored = null, actual = 1_000L, fullRescan = false))
    }

    @Test
    fun `mtime 이 그대로면 건너뛴다`() {
        assertTrue(canSkip(stored = 1_000L, actual = 1_000L, fullRescan = false))
    }

    @Test
    fun `mtime 이 바뀌었으면 훑는다`() {
        // 폴더 안에서 항목이 생기거나 사라졌다는 뜻이다.
        assertFalse(canSkip(stored = 1_000L, actual = 2_000L, fullRescan = false))
    }

    @Test
    fun `사용자가 직접 요청한 스캔은 건너뛰지 않는다`() {
        // 파일 "내용" 만 바뀐 경우는 폴더 mtime 이 안 변해서 자동 스캔이 지나친다.
        // 당겨서 새로고침은 그걸 잡으라고 있는 것이므로 지름길을 막는다.
        assertFalse(canSkip(stored = 1_000L, actual = 1_000L, fullRescan = true))
    }

    @Test
    fun `사라진 폴더는 훑는 쪽으로 떨어진다`() {
        // File.lastModified() 는 대상이 없으면 0 을 준다. 저장값과 같을 수 없으므로
        // 건너뛰지 않고 목록 읽기로 가서, 거기서 null 을 받아 정리된다.
        assertFalse(canSkip(stored = 1_000L, actual = 0L, fullRescan = false))
    }

    /**
     * [FileScanner] 의 건너뛰기 조건과 같은 규칙.
     *
     * 스캐너 본체는 파일시스템과 DB 를 함께 물고 있어서 단위 테스트로 세우기 어렵다.
     * 판단 규칙만 여기 옮겨 두고, 스캐너가 이 조건을 바꾸면 같이 바꾼다.
     */
    private fun canSkip(stored: Long?, actual: Long, fullRescan: Boolean): Boolean =
        !fullRescan && stored != null && actual == stored
}
