package de.leohopper.myturtle.data.backup

import de.leohopper.myturtle.data.HomeCardLayout
import de.leohopper.myturtle.data.local.LifeEventEntity
import de.leohopper.myturtle.data.local.MeasurementEntity
import de.leohopper.myturtle.data.local.PhotoEntity
import de.leohopper.myturtle.data.local.TurtleEntity
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class BackupBundle(
    val backupVersion: Int = CURRENT_BACKUP_VERSION,
    val appId: String = APP_ID,
    val appVersionName: String,
    val exportedAt: String,
    val settings: BackupSettings,
    val turtles: List<BackupTurtle>,
)

@Serializable
data class BackupSettings(
    val homeCardLayout: String = HomeCardLayout.STANDARD.name,
)

@Serializable
data class BackupTurtle(
    val id: Long,
    val name: String,
    val species: String,
    val hatchDate: String? = null,
    val hatchDatePrecision: String? = null,
    val sex: String? = null,
    val notes: String = "",
    val createdAt: Long,
    val trashedAt: Long? = null,
    val measurements: List<BackupMeasurement> = emptyList(),
    val lifeEvents: List<BackupLifeEvent> = emptyList(),
    val photos: List<BackupPhoto> = emptyList(),
)

@Serializable
data class BackupMeasurement(
    val id: Long,
    val date: String,
    val weightGrams: Float? = null,
    val carapaceLengthMm: Float? = null,
    val notes: String = "",
    val photoPaths: List<String> = emptyList(),
)

@Serializable
data class BackupLifeEvent(
    val id: Long,
    val date: String,
    val title: String,
    val category: String = "Allgemein",
    val notes: String = "",
)

@Serializable
data class BackupPhoto(
    val id: Long,
    val year: Int,
    val date: String? = null,
    val caption: String = "",
    val mediaPath: String,
    val addedAt: Long,
)

data class BackupSummary(
    val turtleCount: Int,
    val measurementCount: Int,
    val eventCount: Int,
    val photoCount: Int,
)

data class BackupRestoreData(
    val homeCardLayout: HomeCardLayout,
    val turtles: List<TurtleEntity>,
    val measurements: List<MeasurementEntity>,
    val lifeEvents: List<LifeEventEntity>,
    val photos: List<PhotoEntity>,
    val referencedPhotoUris: Set<String>,
)

fun BackupBundle.summary(): BackupSummary {
    return BackupSummary(
        turtleCount = turtles.size,
        measurementCount = turtles.sumOf { it.measurements.size },
        eventCount = turtles.sumOf { it.lifeEvents.size },
        photoCount = turtles.sumOf { it.photos.size },
    )
}

fun BackupBundle.requireSupported() {
    require(backupVersion == CURRENT_BACKUP_VERSION) {
        "Dieses Backup-Format wird von dieser App-Version noch nicht unterstützt."
    }
    require(appId == APP_ID) {
        "Die ausgewählte Datei ist kein gültiges MyTurtle-Backup."
    }
}

fun BackupBundle.toRestoreData(
    resolveImportedPhotoUri: (String) -> String,
): BackupRestoreData {
    requireSupported()

    val homeCardLayout = HomeCardLayout.entries.firstOrNull { it.name == settings.homeCardLayout }
        ?: HomeCardLayout.STANDARD

    val turtleEntities = mutableListOf<TurtleEntity>()
    val measurementEntities = mutableListOf<MeasurementEntity>()
    val lifeEventEntities = mutableListOf<LifeEventEntity>()
    val photoEntities = mutableListOf<PhotoEntity>()
    val referencedPhotoUris = linkedSetOf<String>()

    turtles.forEach { turtle ->
        turtleEntities += TurtleEntity(
            id = turtle.id,
            name = turtle.name,
            species = turtle.species,
            hatchDate = turtle.hatchDate?.let(LocalDate::parse),
            hatchDatePrecision = turtle.hatchDatePrecision,
            sex = turtle.sex,
            notes = turtle.notes,
            createdAt = turtle.createdAt,
            trashedAt = turtle.trashedAt,
        )

        turtle.measurements.forEach { measurement ->
            val resolvedPhotoUris = measurement.photoPaths.map { mediaPath ->
                resolveImportedPhotoUri(mediaPath).also { referencedPhotoUris += it }
            }

            measurementEntities += MeasurementEntity(
                id = measurement.id,
                turtleId = turtle.id,
                date = LocalDate.parse(measurement.date),
                weightGrams = measurement.weightGrams,
                carapaceLengthMm = measurement.carapaceLengthMm,
                notes = measurement.notes,
                photoUris = resolvedPhotoUris,
            )
        }

        turtle.lifeEvents.forEach { event ->
            lifeEventEntities += LifeEventEntity(
                id = event.id,
                turtleId = turtle.id,
                date = LocalDate.parse(event.date),
                title = event.title,
                category = event.category,
                notes = event.notes,
            )
        }

        turtle.photos.forEach { photo ->
            val contentUri = resolveImportedPhotoUri(photo.mediaPath)
            referencedPhotoUris += contentUri

            photoEntities += PhotoEntity(
                id = photo.id,
                turtleId = turtle.id,
                year = photo.year,
                date = photo.date?.let(LocalDate::parse),
                caption = photo.caption,
                contentUri = contentUri,
                addedAt = photo.addedAt,
            )
        }
    }

    return BackupRestoreData(
        homeCardLayout = homeCardLayout,
        turtles = turtleEntities,
        measurements = measurementEntities,
        lifeEvents = lifeEventEntities,
        photos = photoEntities,
        referencedPhotoUris = referencedPhotoUris,
    )
}

const val CURRENT_BACKUP_VERSION = 1
const val APP_ID = "de.leohopper.myturtle"
