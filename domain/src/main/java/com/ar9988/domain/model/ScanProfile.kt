package com.ar9988.domain.model

/**
 * 스캔 한 번의 시간 내역.
 *
 * 어디가 느린지 모르는 채로 최적화하면 엉뚱한 곳을 깎게 된다.
 * 구간별 누적 시간을 재서, 90초의 내역을 숫자로 보기 위한 것이다.
 *
 * 구간 합계는 전체 시간과 정확히 맞지 않는다. 파일 처리는 여러 스레드에서
 * 동시에 일어나므로 겹쳐서 흐른 시간이 중복으로 잡힌다. 절대값보다 **어느 구간이
 * 큰가**를 보는 용도다.
 */
data class ScanProfile(
    val totalMillis: Long,
    val directories: Int,
    /** 폴더가 그대로여서 목록 읽기를 건너뛴 수. 재스캔에서 이 비율이 곧 절감폭이다. */
    val skippedDirectories: Int,
    val files: Int,
    /**
     * 병렬 구간이 실제로 몇 배로 겹쳐 돌았는지.
     *
     * 스레드 합계를 벽시계로 나눈 값이다. limitedParallelism 을 4 로 두어도
     * 이 값이 1 에 가까우면, 네 스레드가 같은 저장장치를 두고 줄을 서고 있다는 뜻이다.
     */
    val effectiveParallelism: Double,
    val phases: List<PhaseTiming>,
) {
    data class PhaseTiming(
        val name: String,
        val millis: Long,
        val calls: Long,
    ) {
        /** 호출 한 번당 마이크로초. 개별 호출이 비싼지, 횟수가 많은지를 가른다. */
        val microsPerCall: Long
            get() = if (calls == 0L) 0L else millis * 1_000 / calls
    }

    fun format(): String = buildString {
        appendLine("── 스캔 프로파일 ──")
        appendLine("전체 ${totalMillis}ms · 디렉터리 ${directories}개 · 파일 ${files}개")
        if (directories > 0) {
            val skipShare = skippedDirectories * 100 / directories
            appendLine("목록 읽기 건너뜀 ${skippedDirectories}개 (${skipShare}%)")
        }
        appendLine("병렬 구간 실효 배수 %.2f배".format(effectiveParallelism))
        if (directories > 0) {
            appendLine("디렉터리당 평균 ${totalMillis * 1_000 / directories}μs")
        }
        appendLine()

        val widest = phases.maxOfOrNull { it.name.length } ?: 0
        phases.sortedByDescending { it.millis }.forEach { phase ->
            val share = if (totalMillis == 0L) 0 else phase.millis * 100 / totalMillis
            appendLine(
                "%-${widest}s %7dms  %3d%%  호출 %7d회  회당 %5dμs".format(
                    phase.name, phase.millis, share, phase.calls, phase.microsPerCall
                )
            )
        }
    }
}
