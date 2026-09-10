package com.ar9988.domain.model

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 프로파일 출력은 스캔이 끝나는 순간 한 번 찍힌다.
 * 여기서 터지면 90초를 기다린 측정이 통째로 날아가므로, 경계값을 확인해 둔다.
 */
class ScanProfileTest {

    @Test
    fun `구간이 큰 순서로 나온다`() {
        val report = ScanProfile(
            totalMillis = 1_000,
            directories = 10,
            skippedDirectories = 0,
            files = 100,
            effectiveParallelism = 3.2,
            phases = listOf(
                ScanProfile.PhaseTiming("작은구간", millis = 10, calls = 5),
                ScanProfile.PhaseTiming("큰구간", millis = 800, calls = 5),
            )
        ).format()

        assertTrue(report.indexOf("큰구간") < report.indexOf("작은구간"))
    }

    @Test
    fun `아무것도 못 읽은 스캔에서도 터지지 않는다`() {
        // 권한이 없거나 빈 저장소면 전부 0 이다. 0 나누기가 나오면 안 된다.
        val report = ScanProfile(
            totalMillis = 0,
            directories = 0,
            skippedDirectories = 0,
            files = 0,
            effectiveParallelism = 0.0,
            phases = ScanProfileFixtures.emptyPhases()
        ).format()

        assertTrue(report.isNotEmpty())
    }

    @Test
    fun `실효 병렬 배수가 출력에 들어간다`() {
        // 이 숫자를 보려고 계측을 넣은 것이므로 빠지면 안 된다.
        val report = ScanProfile(
            totalMillis = 100,
            directories = 1,
            skippedDirectories = 1,
            files = 1,
            effectiveParallelism = 1.07,
            phases = ScanProfileFixtures.emptyPhases()
        ).format()

        assertTrue(report.contains("1.07"))
    }

    @Test
    fun `호출이 없는 구간은 회당 시간을 0으로 둔다`() {
        val timing = ScanProfile.PhaseTiming("안쓰임", millis = 0, calls = 0)
        assertTrue(timing.microsPerCall == 0L)
    }

    @Test
    fun `회당 시간은 마이크로초로 환산된다`() {
        val timing = ScanProfile.PhaseTiming("구간", millis = 200, calls = 100)
        assertTrue(timing.microsPerCall == 2_000L)
    }
}

private object ScanProfileFixtures {
    fun emptyPhases() = listOf(
        ScanProfile.PhaseTiming("구간A", millis = 0, calls = 0),
        ScanProfile.PhaseTiming("구간B", millis = 0, calls = 0),
    )
}
