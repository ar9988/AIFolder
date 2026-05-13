package com.example.domain.util

import com.example.domain.model.DateRange

object DateParser {
    fun parse(text: String): Pair<DateRange?, String> {
        val calendar = java.util.Calendar.getInstance()

        val patterns = listOf(
            Regex("저번\\s?달|지난\\s?달") to {
                calendar.add(java.util.Calendar.MONTH, -1)
                val start = getMonthStart(calendar)
                val end = getMonthEnd(calendar)
                DateRange(start, end)
            },
            Regex("이번\\s?달|이번\\s?월") to {
                DateRange(getMonthStart(calendar), getMonthEnd(calendar))
            },
            Regex("오늘") to {
                DateRange(getDayStart(calendar), getDayEnd(calendar))
            },
            Regex("어제") to {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                DateRange(getDayStart(calendar), getDayEnd(calendar))
            },
            Regex("이번\\s?주") to {
                calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
                val start = getDayStart(calendar)
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 6)
                DateRange(start, getDayEnd(calendar))
            },
            Regex("저번\\s?주|지난\\s?주") to {
                calendar.add(java.util.Calendar.WEEK_OF_YEAR, -1)
                calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
                val start = getDayStart(calendar)
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 6)
                DateRange(start, getDayEnd(calendar))
            }
        )

        var remaining = text
        var dateRange: DateRange? = null

        for ((pattern, rangeProvider) in patterns) {
            if (pattern.containsMatchIn(remaining)) {
                dateRange = rangeProvider()
                remaining = pattern.replace(remaining, "").trim()
                break
            }
        }

        return Pair(dateRange, remaining)
    }

    private fun getMonthStart(cal: java.util.Calendar): Long {
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        return getDayStart(cal)
    }

    private fun getMonthEnd(cal: java.util.Calendar): Long {
        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
        return getDayEnd(cal)
    }

    private fun getDayStart(cal: java.util.Calendar): Long {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getDayEnd(cal: java.util.Calendar): Long {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}