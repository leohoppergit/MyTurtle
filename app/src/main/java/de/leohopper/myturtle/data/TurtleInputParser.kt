package de.leohopper.myturtle.data

import de.leohopper.myturtle.domain.HatchDateInfo
import de.leohopper.myturtle.domain.HatchDatePrecision
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object TurtleInputParser {

    fun parseOptionalFloat(text: String): Float? {
        val normalized = text.trim().replace(',', '.')
        return normalized.toFloatOrNull()?.takeIf { it > 0f }
    }

    fun parseOptionalDate(text: String): LocalDate? {
        val clean = text.trim()
        if (clean.isBlank()) return null

        return FULL_DATE_FORMATTERS.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(clean, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    fun parseOptionalHatchDate(text: String): HatchDateInfo? {
        val clean = text.trim()
        if (clean.isBlank()) return null

        FULL_DATE_FORMATTERS.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(clean, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        }?.let { date ->
            return HatchDateInfo(
                date = date,
                precision = HatchDatePrecision.DAY,
            )
        }

        val monthYear = MONTH_YEAR_FORMATTERS.firstNotNullOfOrNull { formatter ->
            try {
                YearMonth.parse(clean, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        } ?: parseCompactMonthYear(clean)

        monthYear?.let { parsedMonthYear ->
            return HatchDateInfo(
                date = parsedMonthYear.atDay(1),
                precision = HatchDatePrecision.MONTH,
            )
        }

        return null
    }

    fun isFutureHatchDate(
        hatchDate: HatchDateInfo,
        today: LocalDate = LocalDate.now(),
    ): Boolean {
        return when (hatchDate.precision) {
            HatchDatePrecision.DAY -> hatchDate.date.isAfter(today)
            HatchDatePrecision.MONTH -> YearMonth.from(hatchDate.date).isAfter(YearMonth.from(today))
        }
    }

    private fun parseCompactMonthYear(text: String): YearMonth? {
        val match = SHORT_MONTH_YEAR_REGEX.matchEntire(text) ?: return null
        val month = match.groupValues[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
        val shortYear = match.groupValues[2].toIntOrNull() ?: return null

        return runCatching {
            YearMonth.of(2000 + shortYear, month)
        }.getOrNull()
    }

    private val FULL_DATE_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ISO_LOCAL_DATE,
    )

    private val MONTH_YEAR_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("MM-uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("MM.uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("MM/uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("M-uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("M.uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("M/uuuu").withResolverStyle(ResolverStyle.STRICT),
    )

    private val SHORT_MONTH_YEAR_REGEX = Regex("""^\s*(\d{1,2})\s*[/.-]\s*(\d{2})\s*$""")
}
