package com.kotlinclasses

import android.os.Build
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// Type aliases for better readability
typealias DateDifference = Long
typealias DateRange = Pair<LocalDate, LocalDate>
typealias TimeUnit = ChronoUnit

/**
 * Extension functions for date and time calculations.
 */

// Extension functions for LocalDate
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.getPrevious(): TPDateExt.PreviousTimeBuilder {
    return TPDateExt().getPrevious(this)
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.getAhead(count: Int): TPDateExt.AheadTimeBuilder {
    return TPDateExt().getAhead(count, this)
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.getCurrent(): TPDateExt.CurrentTimeBuilder {
    return TPDateExt().getCurrent(this)
}

// Extension property for getting time calculations
val LocalDate.timeCalculations: Map<String, Long>
    @RequiresApi(Build.VERSION_CODES.O)
    get() = TPDateExt().getTimeCalculations(this.year)

/**
 * Utility class for date calculations and manipulations in Android.
 * Provides various methods for working with dates, including:
 * - Time calculations (months, weeks, days, etc.)
 * - Previous time calculations
 * - Future date calculations
 * - Current date information
 *
 * @property currentDate The current date used as reference
 */
@RequiresApi(Build.VERSION_CODES.O)
class TPDateExt {
    private val currentDate = LocalDate.now()

    /**
     * Gets time calculations from the start of a year until a target date.
     * @param year The year to calculate from, defaults to current year
     * @return Map containing various time calculations (year, month, weeks, days, hours, minutes, seconds)
     */
    fun getTimeCalculations(@IntRange(from = 1900, to = 9999) year: Int = currentDate.year): Map<String, Long> {
        val targetDate = if (year == currentDate.year) {
            currentDate
        } else {
            LocalDate.of(year, 12, 31)
        }
      
        val startOfYear = LocalDate.of(year, 1, 1)
        val startOfMonth = LocalDate.of(year, targetDate.monthValue, 1)

        return mapOf(
            "year" to year.toLong(),
            "month" to targetDate.monthValue.toLong(),
            "weeks" to ChronoUnit.WEEKS.between(startOfYear, targetDate),
            "days" to ChronoUnit.DAYS.between(startOfYear, targetDate),
            "hours" to ChronoUnit.DAYS.between(startOfYear, targetDate) * 24,
            "minutes" to ChronoUnit.DAYS.between(startOfYear, targetDate) * 24 * 60,
            "seconds" to ChronoUnit.DAYS.between(startOfYear, targetDate) * 24 * 60 * 60
        )
    }

    enum class TimeUnit {
        MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS
    }

    inner class PreviousTimeBuilder(
        private val targetDate: LocalDate,
        private val comparisonDate: LocalDate = currentDate
    ) {
        fun Months(): Int {
            return ChronoUnit.MONTHS.between(targetDate, comparisonDate).toInt()
        }

        fun Weeks(): Int {
            return ChronoUnit.WEEKS.between(targetDate, comparisonDate).toInt()
        }

        fun Days(): Int {
            return ChronoUnit.DAYS.between(targetDate, comparisonDate).toInt()
        }

        fun Hours(): Long {
            return Days().toLong() * 24
        }

        fun Minutes(): Long {
            return Hours() * 60
        }

        fun Seconds(): Long {
            return Minutes() * 60
        }

        fun Years(): Long = ChronoUnit.YEARS.between(targetDate, comparisonDate)
    }

    /**
     * Gets a PreviousTimeBuilder for calculating time differences.
     * @param targetDate The date to measure from
     * @param comparisonDate The date to measure to (defaults to current date)
     * @return PreviousTimeBuilder instance
     */
    @WorkerThread
    fun getPrevious(targetDate: LocalDate, comparisonDate: LocalDate = currentDate): PreviousTimeBuilder {
        return PreviousTimeBuilder(targetDate, comparisonDate)
    }

    // Convenience method for year-based comparison
    fun getPrevious(year: Int = currentDate.year): PreviousTimeBuilder {
        val targetDate = LocalDate.of(year, 1, 1)
        return PreviousTimeBuilder(targetDate)
    }

    fun Years(): Long = ChronoUnit.YEARS.between(LocalDate.of(1970, 1, 1), currentDate)

    inner class AheadTimeBuilder(
        private val startDate: LocalDate = currentDate,
        private val count: Int
    ) {
        fun Years(): LocalDate {
            return startDate.plusYears(count.toLong())
        }

        fun Months(): LocalDate {
            return startDate.plusMonths(count.toLong())
        }

        fun Weeks(): LocalDate {
            return startDate.plusWeeks(count.toLong())
        }

        fun Days(): LocalDate {
            return startDate.plusDays(count.toLong())
        }

        fun Hours(): LocalDate {
            return startDate.plusDays(count / 24L)
        }

        fun Minutes(): LocalDate {
            return startDate.plusDays(count / (24 * 60L))
        }

        fun Seconds(): LocalDate {
            return startDate.plusDays(count / (24 * 60 * 60L))
        }
    }

    /**
     * Gets an AheadTimeBuilder for calculating future dates.
     * @param count The number of units to add
     * @param startDate The starting date (defaults to current date)
     * @return AheadTimeBuilder instance
     */
    @WorkerThread
    fun getAhead(count: Int, startDate: LocalDate = currentDate): AheadTimeBuilder {
        return AheadTimeBuilder(startDate, count)
    }

    inner class CurrentTimeBuilder(private val date: LocalDate = currentDate) {
        fun Year(): Int {
            return date.year
        }

        fun Quarter(): Int {
            return (date.monthValue - 1) / 3 + 1
        }

        fun Month(): Int {
            return date.monthValue
        }

        fun Week(): Int {
            return (date.dayOfYear - 1) / 7 + 1
        }

        fun Day(): Int {
            return date.dayOfYear
        }

        fun Date(format: String = "yyyy-MM-dd"): String {
            return date.format(java.time.format.DateTimeFormatter.ofPattern(format))
        }

        fun Years(): Long = ChronoUnit.YEARS.between(LocalDate.of(1970, 1, 1), date)
    }

    /**
     * Gets a CurrentTimeBuilder for current date information.
     * @param date The date to get information for (defaults to current date)
     * @return CurrentTimeBuilder instance
     */
    fun getCurrent(date: LocalDate = currentDate): CurrentTimeBuilder {
        return CurrentTimeBuilder(date)
    }
}