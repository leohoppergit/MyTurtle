package de.leohopper.myturtle.data

import android.net.Uri
import de.leohopper.myturtle.data.local.LifeEventEntity
import de.leohopper.myturtle.data.local.MeasurementEntity
import de.leohopper.myturtle.data.local.PhotoEntity
import de.leohopper.myturtle.data.local.TurtleDao
import de.leohopper.myturtle.data.local.TurtleEntity
import de.leohopper.myturtle.data.local.TurtleWithDetails
import de.leohopper.myturtle.domain.HatchDateInfo
import de.leohopper.myturtle.domain.HatchDatePrecision
import de.leohopper.myturtle.domain.LifeEventRecord
import de.leohopper.myturtle.domain.MeasurementRecord
import de.leohopper.myturtle.domain.PhotoRecord
import de.leohopper.myturtle.domain.TurtleDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TurtleRepository(
    private val turtleDao: TurtleDao,
    private val mediaStore: TurtleMediaStore,
) {

    fun observeTurtles(): Flow<List<TurtleDetails>> {
        return turtleDao.observeTurtles().map { turtles ->
            turtles.map { it.toDomain() }
        }
    }

    fun observeTrashedTurtles(): Flow<List<TurtleDetails>> {
        return turtleDao.observeTrashedTurtles().map { turtles ->
            turtles.map { it.toDomain() }
        }
    }

    suspend fun addTurtle(
        name: String,
        species: String,
        hatchDate: HatchDateInfo?,
        sex: String?,
        notes: String,
    ) {
        turtleDao.insertTurtle(
            TurtleEntity(
                name = name,
                species = species,
                hatchDate = hatchDate?.date,
                hatchDatePrecision = hatchDate?.precision?.name,
                sex = sex,
                notes = notes,
            ),
        )
    }

    suspend fun updateTurtle(
        id: Long,
        name: String,
        species: String,
        hatchDate: HatchDateInfo?,
        sex: String?,
        notes: String,
    ) {
        val existing = turtleDao.getTurtleById(id) ?: return
        turtleDao.updateTurtle(
            TurtleEntity(
                id = id,
                name = name,
                species = species,
                hatchDate = hatchDate?.date,
                hatchDatePrecision = hatchDate?.precision?.name,
                sex = sex,
                notes = notes,
                createdAt = existing.createdAt,
                trashedAt = existing.trashedAt,
            ),
        )
    }

    suspend fun addMeasurement(
        turtleId: Long,
        date: LocalDate,
        weightGrams: Float?,
        carapaceLengthMm: Float?,
        notes: String,
        photoUris: List<String>,
    ) {
        val stablePhotoUris = photoUris.toList()
        turtleDao.insertMeasurement(
            MeasurementEntity(
                turtleId = turtleId,
                date = date,
                weightGrams = weightGrams,
                carapaceLengthMm = carapaceLengthMm,
                notes = notes,
                photoUris = stablePhotoUris,
            ),
        )
    }

    suspend fun addLifeEvent(
        turtleId: Long,
        date: LocalDate,
        title: String,
        notes: String,
    ) {
        turtleDao.insertLifeEvent(
            LifeEventEntity(
                turtleId = turtleId,
                date = date,
                title = title,
                notes = notes,
            ),
        )
    }

    suspend fun addPhoto(
        turtleId: Long,
        year: Int,
        date: LocalDate?,
        caption: String,
        contentUri: String,
    ) {
        turtleDao.insertPhoto(
            PhotoEntity(
                turtleId = turtleId,
                year = year,
                date = date,
                caption = caption,
                contentUri = contentUri,
            ),
        )
    }

    fun createCameraCaptureTarget(): CameraCaptureTarget = mediaStore.createCameraCaptureTarget()

    suspend fun importMeasurementPhoto(sourceUri: Uri): String = mediaStore.importMeasurementPhoto(sourceUri)

    suspend fun importYearPhoto(sourceUri: Uri): String = mediaStore.importYearPhoto(sourceUri)

    fun discardImportedPhoto(uriString: String?) = mediaStore.discardImportedPhoto(uriString)

    fun discardImportedPhotos(uriStrings: List<String>) {
        uriStrings.forEach(mediaStore::discardImportedPhoto)
    }

    fun cleanupTemporaryCapture(cleanupPath: String?) = mediaStore.cleanupTemporaryCapture(cleanupPath)

    suspend fun moveTurtleToTrash(turtleId: Long) {
        turtleDao.updateTurtleTrashState(
            turtleId = turtleId,
            trashedAt = System.currentTimeMillis(),
        )
    }

    suspend fun restoreTurtle(turtleId: Long) {
        turtleDao.updateTurtleTrashState(
            turtleId = turtleId,
            trashedAt = null,
        )
    }

    suspend fun deleteTurtleFromTrash(turtleId: Long) {
        val turtle = turtleDao.getTrashedTurtleById(turtleId) ?: return
        purgeTurtles(listOf(turtle))
    }

    suspend fun emptyTrash(): Int = purgeTurtles(turtleDao.getAllTrashedTurtles())

    suspend fun purgeExpiredTrash(nowMillis: Long = System.currentTimeMillis()): Int {
        val cutoff = nowMillis - TRASH_RETENTION_MILLIS
        return purgeTurtles(turtleDao.getExpiredTrashedTurtles(cutoff))
    }

    suspend fun deleteMeasurement(measurementId: Long) {
        val photoUris = turtleDao.getMeasurementById(measurementId)?.photoUris.orEmpty()
        turtleDao.deleteMeasurementById(measurementId)
        photoUris.forEach(mediaStore::discardImportedPhoto)
    }

    suspend fun deleteLifeEvent(eventId: Long) = turtleDao.deleteLifeEventById(eventId)

    suspend fun deletePhoto(photoId: Long) {
        val photoUri = turtleDao.getPhotoUri(photoId)
        turtleDao.deletePhotoById(photoId)
        mediaStore.discardImportedPhoto(photoUri)
    }

    private suspend fun purgeTurtles(turtles: List<TurtleWithDetails>): Int {
        turtles.forEach { turtle ->
            val photoUris = buildList {
                addAll(turtle.measurements.flatMap { it.photoUris })
                addAll(turtle.photos.map { it.contentUri })
            }.distinct()

            turtleDao.deleteTurtleById(turtle.turtle.id)
            photoUris.forEach(mediaStore::discardImportedPhoto)
        }
        return turtles.size
    }

    companion object {
        private const val TRASH_RETENTION_MILLIS = 30L * 24L * 60L * 60L * 1000L
    }
}

private fun TurtleWithDetails.toDomain(): TurtleDetails {
    return TurtleDetails(
        id = turtle.id,
        name = turtle.name,
        species = turtle.species,
        hatchDate = turtle.hatchDate?.let { date ->
            HatchDateInfo(
                date = date,
                precision = turtle.hatchDatePrecision.toHatchDatePrecision(),
            )
        },
        sex = turtle.sex,
        notes = turtle.notes,
        trashedAt = turtle.trashedAt,
        measurements = measurements
            .sortedBy { it.date }
            .map { measurement ->
                MeasurementRecord(
                    id = measurement.id,
                    date = measurement.date,
                    weightGrams = measurement.weightGrams,
                    carapaceLengthMm = measurement.carapaceLengthMm,
                    notes = measurement.notes,
                    photoUris = measurement.photoUris,
                )
            },
        lifeEvents = lifeEvents
            .sortedBy { it.date }
            .map { event ->
                LifeEventRecord(
                    id = event.id,
                    date = event.date,
                    title = event.title,
                    category = event.category,
                    notes = event.notes,
                )
            },
        photos = photos
            .sortedWith(compareBy<PhotoEntity>({ it.year }, { it.date ?: LocalDate.MIN }, { it.addedAt }))
            .map { photo ->
                PhotoRecord(
                    id = photo.id,
                    year = photo.year,
                    date = photo.date,
                    caption = photo.caption,
                    contentUri = photo.contentUri,
                )
            },
    )
}

private fun String?.toHatchDatePrecision(): HatchDatePrecision {
    return when (this) {
        HatchDatePrecision.MONTH.name -> HatchDatePrecision.MONTH
        else -> HatchDatePrecision.DAY
    }
}
