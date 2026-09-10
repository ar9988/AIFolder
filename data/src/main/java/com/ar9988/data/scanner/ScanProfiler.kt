package com.ar9988.data.scanner

import com.ar9988.domain.model.ScanProfile
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLongArray

/**
 * 스캔 구간별 누적 시간을 잰다.
 *
 * 계측 자체가 결과를 왜곡하면 안 되므로 할당을 하지 않는다 —
 * 람다도, 객체도 만들지 않고 [add] 로 숫자만 더한다.
 * nanoTime 호출은 20~30ns 수준이라 파일 20만 개 기준 수십 ms 안쪽이다.
 *
 * 파일 처리는 여러 스레드에서 동시에 돌기 때문에 원자적 누적을 쓴다.
 * 그만큼 구간 합계는 벽시계 시간보다 커질 수 있다 — 겹쳐 흐른 시간이 중복으로 잡힌다.
 */
class ScanProfiler {

    enum class Phase(val label: String) {
        DIR_MTIME_CHECK("폴더 변경 확인"),
        LIST_DIR("디렉터리 나열(스레드 합계)"),
        LIST_WAIT("└ 그중 기다린 시간"),
        FILTER("제외 규칙 필터"),
        DB_READ("DB 읽기(기존 목록)"),
        FILE_STAT("└ 그중 메타데이터 읽기"),
        FILE_PROCESS("파일 처리(스레드 합계)"),
        PARALLEL_SPAN("└ 병렬 구간 벽시계"),
        FILE_HASH("부분 해시"),
        DB_INSERT_DIR("DB 폴더 즉시 삽입"),
        DB_WRITE("DB 일괄 쓰기"),
        DB_DELETE("DB 삭제"),
        EVENT_SEND("이벤트 전송"),
    }

    private val nanos = AtomicLongArray(Phase.entries.size)
    private val calls = AtomicLongArray(Phase.entries.size)
    private val directories = AtomicInteger()
    private val skippedDirectories = AtomicInteger()
    private val files = AtomicInteger()

    private var startedAt = 0L

    fun start() {
        startedAt = System.nanoTime()
        for (i in 0 until nanos.length()) {
            nanos.set(i, 0L)
            calls.set(i, 0L)
        }
        directories.set(0)
        skippedDirectories.set(0)
        files.set(0)
    }

    /** [since] 는 구간 시작 시점의 nanoTime. */
    fun add(phase: Phase, since: Long) {
        nanos.getAndAdd(phase.ordinal, System.nanoTime() - since)
        calls.getAndIncrement(phase.ordinal)
    }

    fun countDirectory() {
        directories.incrementAndGet()
    }

    fun countFiles(n: Int) {
        files.addAndGet(n)
    }

    /** 폴더가 그대로여서 목록 읽기를 건너뛴 경우. */
    fun countSkippedDirectory() {
        skippedDirectories.incrementAndGet()
    }

    fun snapshot(): ScanProfile = ScanProfile(
        totalMillis = (System.nanoTime() - startedAt) / 1_000_000,
        directories = directories.get(),
        skippedDirectories = skippedDirectories.get(),
        files = files.get(),
        // 스레드 합계 ÷ 벽시계 = 평균 몇 개의 코루틴이 실제로 돌고 있었는가.
        //
        // 분자는 FILE_PROCESS 여야 한다. FILE_STAT 은 processFile 의 일부만 덮기 때문에,
        // 그걸로 나누면 나머지 작업만큼 배수가 낮게 나온다.
        // 대기열에서 기다린 시간은 어차피 코루틴이 시작된 뒤부터 재므로 포함되지 않는다.
        effectiveParallelism = nanos.get(Phase.PARALLEL_SPAN.ordinal)
            .takeIf { it > 0 }
            ?.let { nanos.get(Phase.FILE_PROCESS.ordinal).toDouble() / it }
            ?: 0.0,
        phases = Phase.entries.map { phase ->
            ScanProfile.PhaseTiming(
                name = phase.label,
                millis = nanos.get(phase.ordinal) / 1_000_000,
                calls = calls.get(phase.ordinal)
            )
        }
    )
}
