package de.leohopper.myturtle.data.local

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `local date roundtrip stays stable`() {
        val date = LocalDate.of(2026, 4, 9)

        val encoded = converters.fromLocalDate(date)
        val decoded = converters.toLocalDate(encoded)

        assertEquals(date, decoded)
    }

    @Test
    fun `string list roundtrip keeps non blank entries`() {
        val values = listOf("file://one.jpg", "", "file://two.jpg")

        val encoded = converters.fromStringList(values)
        val decoded = converters.toStringList(encoded)

        assertEquals(listOf("file://one.jpg", "file://two.jpg"), decoded)
    }

    @Test
    fun `blank converter input becomes empty list`() {
        assertEquals(emptyList<String>(), converters.toStringList(""))
        assertEquals(emptyList<String>(), converters.toStringList(null))
    }
}
