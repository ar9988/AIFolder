package com.ar9988.domain.util

import com.ar9988.domain.model.DateRange
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateParserTest {
    private val seoul = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(Instant.parse("2026-09-13T12:00:00Z"), seoul)

    @Test
    fun `English and Korean month expressions produce the same calendar range`() {
        for (phrase in listOf("last month", "previous month", "LAST   MONTH", "지난달", "지난 달", "저번달")) {
            assertEquals(phrase, range("2026-08-01", "2026-09-01"), DateParser.parse(phrase, clock).first)
        }
        for (phrase in listOf("this month", "current month", "이번달", "이번 월")) {
            assertEquals(phrase, range("2026-09-01", "2026-10-01"), DateParser.parse(phrase, clock).first)
        }
    }

    @Test
    fun `possessive month expressions leave only the search request`() {
        for (query in listOf("Find last month's receipts", "Find last month’s receipts", "Find LAST MONTH receipts")) {
            val (dates, remaining) = DateParser.parse(query, clock)
            assertEquals(range("2026-08-01", "2026-09-01"), dates)
            assertEquals("Find receipts", remaining)
            assertEquals(listOf("receipts"), AssistantQueryCleaner.clean(remaining))
        }
    }

    @Test
    fun `last month handles year rollover and leap February`() {
        val january = Clock.fixed(Instant.parse("2026-01-31T12:00:00Z"), seoul)
        val leapMarch = Clock.fixed(Instant.parse("2024-03-31T12:00:00Z"), seoul)
        assertEquals(range("2025-12-01", "2026-01-01"), DateParser.parse("last month", january).first)
        assertEquals(range("2024-02-01", "2024-03-01"), DateParser.parse("지난달", leapMarch).first)
    }

    @Test
    fun `weeks start on Monday even when today is Sunday`() {
        for (phrase in listOf("this week", "current week", "이번주")) {
            assertEquals(phrase, range("2026-09-07", "2026-09-14"), DateParser.parse(phrase, clock).first)
        }
        for (phrase in listOf("last week", "previous week", "지난주", "저번 주")) {
            assertEquals(phrase, range("2026-08-31", "2026-09-07"), DateParser.parse(phrase, clock).first)
        }
        val monday = Clock.fixed(Instant.parse("2026-09-14T00:00:00Z"), seoul)
        assertEquals(range("2026-09-14", "2026-09-21"), DateParser.parse("this week", monday).first)
    }

    @Test
    fun `today and yesterday accept both languages`() {
        for (phrase in listOf("today", "TODAY'S", "오늘")) {
            assertEquals(range("2026-09-13", "2026-09-14"), DateParser.parse(phrase, clock).first)
        }
        for (phrase in listOf("yesterday", "yesterday’s", "어제")) {
            assertEquals(range("2026-09-12", "2026-09-13"), DateParser.parse(phrase, clock).first)
        }
    }

    @Test
    fun `local day boundaries follow daylight saving and timezone`() {
        val newYork = ZoneId.of("America/New_York")
        val spring = Clock.fixed(Instant.parse("2026-03-08T12:00:00Z"), newYork)
        val fall = Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), newYork)
        val springRange = DateParser.parse("today", spring).first!!
        val fallRange = DateParser.parse("today", fall).first!!
        assertEquals(range("2026-03-08", "2026-03-09", newYork), springRange)
        assertEquals(23L * 60 * 60 * 1000, springRange.end - springRange.start + 1)
        assertEquals(25L * 60 * 60 * 1000, fallRange.end - fallRange.start + 1)
        val utc = Clock.fixed(Instant.parse("2026-09-13T23:30:00Z"), ZoneId.of("UTC"))
        assertEquals(range("2026-09-14", "2026-09-15"), DateParser.parse("today", utc.withZone(seoul)).first)
    }

    @Test
    fun `date-only English example does not require a matching tag`() {
        val (dates, remaining) = DateParser.parse("Show files I worked on this week", clock)
        assertEquals(range("2026-09-07", "2026-09-14"), dates)
        assertEquals(emptyList<String>(), AssistantQueryCleaner.clean(remaining))
    }

    @Test
    fun `ordinary words and unsupported dates are not partially matched`() {
        for (query in listOf("last monthly report", "yesterdaysong", "todayreport", "receipts", "next month")) {
            val (dates, remaining) = DateParser.parse(query, clock)
            assertNull(query, dates)
            assertEquals(query, remaining)
        }
    }

    @Test
    fun `Korean query continues to retain its tag keyword`() {
        val (_, remaining) = DateParser.parse("지난달 영수증 찾아줘", clock)
        assertEquals(listOf("영수증"), AssistantQueryCleaner.clean(remaining))
    }

    private fun range(start: String, endExclusive: String, zone: ZoneId = seoul) = DateRange(
        LocalDate.parse(start).atStartOfDay(zone).toInstant().toEpochMilli(),
        LocalDate.parse(endExclusive).atStartOfDay(zone).toInstant().toEpochMilli() - 1,
    )
}
