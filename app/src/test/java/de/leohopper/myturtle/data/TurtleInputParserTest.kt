package de.leohopper.myturtle.data

import de.leohopper.myturtle.domain.HatchDatePrecision
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TurtleInputParserTest {

    @Test
    fun `parse full date accepts german day month year`() {
        val parsed = TurtleInputParser.parseOptionalDate("09-04-2026")

        assertEquals(LocalDate.of(2026, 4, 9), parsed)
    }

    @Test
    fun `parse hatch date accepts month year with full year`() {
        val parsed = TurtleInputParser.parseOptionalHatchDate("08-2022")

        requireNotNull(parsed)
        assertEquals(LocalDate.of(2022, 8, 1), parsed.date)
        assertEquals(HatchDatePrecision.MONTH, parsed.precision)
    }

    @Test
    fun `parse hatch date accepts short month year`() {
        val parsed = TurtleInputParser.parseOptionalHatchDate("08/26")

        requireNotNull(parsed)
        assertEquals(LocalDate.of(2026, 8, 1), parsed.date)
        assertEquals(HatchDatePrecision.MONTH, parsed.precision)
    }

    @Test
    fun `future hatch month is detected correctly`() {
        val hatchDate = TurtleInputParser.parseOptionalHatchDate("08-2026")

        requireNotNull(hatchDate)
        assertTrue(
            TurtleInputParser.isFutureHatchDate(
                hatchDate = hatchDate,
                today = LocalDate.of(2026, 4, 9),
            ),
        )
        assertFalse(
            TurtleInputParser.isFutureHatchDate(
                hatchDate = hatchDate,
                today = LocalDate.of(2026, 8, 15),
            ),
        )
    }

    @Test
    fun `parse float accepts comma and rejects zero`() {
        assertEquals(37.5f, TurtleInputParser.parseOptionalFloat("37,5"))
        assertNull(TurtleInputParser.parseOptionalFloat("0"))
        assertNull(TurtleInputParser.parseOptionalFloat("abc"))
    }
}
