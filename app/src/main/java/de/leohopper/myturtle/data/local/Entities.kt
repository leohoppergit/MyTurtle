package de.leohopper.myturtle.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

@Entity(tableName = "turtles")
data class TurtleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val species: String,
    val hatchDate: LocalDate? = null,
    val hatchDatePrecision: String? = null,
    val sex: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val trashedAt: Long? = null,
)

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = TurtleEntity::class,
            parentColumns = ["id"],
            childColumns = ["turtleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("turtleId")],
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val turtleId: Long,
    val date: LocalDate,
    val weightGrams: Float? = null,
    val carapaceLengthMm: Float? = null,
    val notes: String = "",
    val photoUris: List<String> = emptyList(),
)

@Entity(
    tableName = "life_events",
    foreignKeys = [
        ForeignKey(
            entity = TurtleEntity::class,
            parentColumns = ["id"],
            childColumns = ["turtleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("turtleId")],
)
data class LifeEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val turtleId: Long,
    val date: LocalDate,
    val title: String,
    val category: String = "Allgemein",
    val notes: String = "",
)

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = TurtleEntity::class,
            parentColumns = ["id"],
            childColumns = ["turtleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("turtleId")],
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val turtleId: Long,
    val year: Int,
    val date: LocalDate? = null,
    val caption: String = "",
    val contentUri: String,
    val addedAt: Long = System.currentTimeMillis(),
)

data class TurtleWithDetails(
    @Embedded
    val turtle: TurtleEntity,
    @Relation(parentColumn = "id", entityColumn = "turtleId")
    val measurements: List<MeasurementEntity>,
    @Relation(parentColumn = "id", entityColumn = "turtleId")
    val lifeEvents: List<LifeEventEntity>,
    @Relation(parentColumn = "id", entityColumn = "turtleId")
    val photos: List<PhotoEntity>,
)
