package de.leohopper.myturtle.domain

import java.time.LocalDate

enum class HatchDatePrecision {
    DAY,
    MONTH,
}

data class HatchDateInfo(
    val date: LocalDate,
    val precision: HatchDatePrecision,
)

data class TurtleDetails(
    val id: Long,
    val name: String,
    val species: String,
    val hatchDate: HatchDateInfo?,
    val sex: String?,
    val notes: String,
    val trashedAt: Long?,
    val measurements: List<MeasurementRecord>,
    val lifeEvents: List<LifeEventRecord>,
    val photos: List<PhotoRecord>,
)

data class MeasurementRecord(
    val id: Long,
    val date: LocalDate,
    val weightGrams: Float?,
    val carapaceLengthMm: Float?,
    val notes: String,
    val photoUris: List<String>,
)

data class LifeEventRecord(
    val id: Long,
    val date: LocalDate,
    val title: String,
    val category: String,
    val notes: String,
)

data class PhotoRecord(
    val id: Long,
    val year: Int,
    val date: LocalDate?,
    val caption: String,
    val contentUri: String,
)
