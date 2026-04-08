package de.leohopper.myturtle.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {

    private val separator = '\u001F'

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromStringList(values: List<String>?): String {
        return values
            ?.filter { it.isNotBlank() }
            ?.joinToString(separator.toString())
            .orEmpty()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(separator).filter { it.isNotBlank() }
    }
}
