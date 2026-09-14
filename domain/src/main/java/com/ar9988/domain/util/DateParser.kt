package com.ar9988.domain.util

import com.ar9988.domain.model.DateRange
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object DateParser {
    private enum class Period { LAST_MONTH, THIS_MONTH, TODAY, YESTERDAY, THIS_WEEK, LAST_WEEK }

    private fun pattern(korean: String, english: String) =
        Regex("(?:$korean)|\\b(?:$english)(?:['’]s)?\\b", RegexOption.IGNORE_CASE)

    private val patterns = listOf(
        pattern("(?:저번|지난)\\s*달", "(?:last|previous)\\s+month") to Period.LAST_MONTH,
        pattern("이번\\s*(?:달|월)", "(?:this|current)\\s+month") to Period.THIS_MONTH,
        pattern("오늘", "today") to Period.TODAY,
        pattern("어제", "yesterday") to Period.YESTERDAY,
        pattern("이번\\s*주", "(?:this|current)\\s+week") to Period.THIS_WEEK,
        pattern("(?:저번|지난)\\s*주", "(?:last|previous)\\s+week") to Period.LAST_WEEK,
    )

    fun parse(text: String, clock: Clock = Clock.systemDefaultZone()): Pair<DateRange?, String> {
        val (pattern, period) = patterns.firstOrNull { it.first.containsMatchIn(text) }
            ?: return null to text
        val today = LocalDate.now(clock)
        val start = when (period) {
            Period.LAST_MONTH -> today.withDayOfMonth(1).minusMonths(1)
            Period.THIS_MONTH -> today.withDayOfMonth(1)
            Period.TODAY -> today
            Period.YESTERDAY -> today.minusDays(1)
            Period.THIS_WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            Period.LAST_WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1)
        }
        val endExclusive = when (period) {
            Period.LAST_MONTH, Period.THIS_MONTH -> start.plusMonths(1)
            Period.THIS_WEEK, Period.LAST_WEEK -> start.plusWeeks(1)
            Period.TODAY, Period.YESTERDAY -> start.plusDays(1)
        }
        // The database uses inclusive millisecond bounds. Local day boundaries also
        // account for daylight-saving transitions without assuming a 24-hour day.
        val range = DateRange(
            start.atStartOfDay(clock.zone).toInstant().toEpochMilli(),
            endExclusive.atStartOfDay(clock.zone).toInstant().toEpochMilli() - 1,
        )
        val remaining = pattern.replace(text, " ").replace(Regex("\\s+"), " ").trim()
        return range to remaining
    }
}
