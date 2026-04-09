package de.leohopper.myturtle.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TurtleDao {

    @Transaction
    @Query("SELECT * FROM turtles WHERE trashedAt IS NULL ORDER BY name COLLATE NOCASE ASC")
    fun observeTurtles(): Flow<List<TurtleWithDetails>>

    @Transaction
    @Query("SELECT * FROM turtles WHERE trashedAt IS NOT NULL ORDER BY trashedAt DESC, name COLLATE NOCASE ASC")
    fun observeTrashedTurtles(): Flow<List<TurtleWithDetails>>

    @Transaction
    @Query("SELECT * FROM turtles ORDER BY id ASC")
    suspend fun getAllTurtlesForBackup(): List<TurtleWithDetails>

    @Insert
    suspend fun insertTurtle(turtle: TurtleEntity): Long

    @Insert
    suspend fun insertTurtles(turtles: List<TurtleEntity>)

    @Update
    suspend fun updateTurtle(turtle: TurtleEntity)

    @Query("SELECT * FROM turtles WHERE id = :turtleId LIMIT 1")
    suspend fun getTurtleById(turtleId: Long): TurtleEntity?

    @Query("UPDATE turtles SET trashedAt = :trashedAt WHERE id = :turtleId")
    suspend fun updateTurtleTrashState(turtleId: Long, trashedAt: Long?)

    @Insert
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long

    @Insert
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>)

    @Insert
    suspend fun insertLifeEvent(event: LifeEventEntity): Long

    @Insert
    suspend fun insertLifeEvents(events: List<LifeEventEntity>)

    @Insert
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Insert
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Query("SELECT * FROM measurements WHERE turtleId = :turtleId")
    suspend fun getMeasurementsByTurtleId(turtleId: Long): List<MeasurementEntity>

    @Query("SELECT contentUri FROM photos WHERE turtleId = :turtleId")
    suspend fun getPhotoUrisByTurtleId(turtleId: Long): List<String>

    @Query("SELECT * FROM measurements WHERE id = :measurementId LIMIT 1")
    suspend fun getMeasurementById(measurementId: Long): MeasurementEntity?

    @Transaction
    @Query("SELECT * FROM turtles WHERE id = :turtleId AND trashedAt IS NOT NULL LIMIT 1")
    suspend fun getTrashedTurtleById(turtleId: Long): TurtleWithDetails?

    @Transaction
    @Query("SELECT * FROM turtles WHERE trashedAt IS NOT NULL")
    suspend fun getAllTrashedTurtles(): List<TurtleWithDetails>

    @Transaction
    @Query("SELECT * FROM turtles WHERE trashedAt IS NOT NULL AND trashedAt <= :cutoff")
    suspend fun getExpiredTrashedTurtles(cutoff: Long): List<TurtleWithDetails>

    @Query("SELECT contentUri FROM photos WHERE id = :photoId")
    suspend fun getPhotoUri(photoId: Long): String?

    @Query("DELETE FROM turtles WHERE id = :turtleId")
    suspend fun deleteTurtleById(turtleId: Long)

    @Query("DELETE FROM measurements WHERE id = :measurementId")
    suspend fun deleteMeasurementById(measurementId: Long)

    @Query("DELETE FROM life_events WHERE id = :eventId")
    suspend fun deleteLifeEventById(eventId: Long)

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)

    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()

    @Query("DELETE FROM life_events")
    suspend fun deleteAllLifeEvents()

    @Query("DELETE FROM measurements")
    suspend fun deleteAllMeasurements()

    @Query("DELETE FROM turtles")
    suspend fun deleteAllTurtles()

    @Transaction
    suspend fun replaceAllData(
        turtles: List<TurtleEntity>,
        measurements: List<MeasurementEntity>,
        lifeEvents: List<LifeEventEntity>,
        photos: List<PhotoEntity>,
    ) {
        deleteAllPhotos()
        deleteAllLifeEvents()
        deleteAllMeasurements()
        deleteAllTurtles()

        if (turtles.isNotEmpty()) insertTurtles(turtles)
        if (measurements.isNotEmpty()) insertMeasurements(measurements)
        if (lifeEvents.isNotEmpty()) insertLifeEvents(lifeEvents)
        if (photos.isNotEmpty()) insertPhotos(photos)
    }
}
