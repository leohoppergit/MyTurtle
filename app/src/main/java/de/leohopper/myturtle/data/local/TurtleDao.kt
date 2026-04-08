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

    @Insert
    suspend fun insertTurtle(turtle: TurtleEntity): Long

    @Update
    suspend fun updateTurtle(turtle: TurtleEntity)

    @Query("SELECT * FROM turtles WHERE id = :turtleId LIMIT 1")
    suspend fun getTurtleById(turtleId: Long): TurtleEntity?

    @Query("UPDATE turtles SET trashedAt = :trashedAt WHERE id = :turtleId")
    suspend fun updateTurtleTrashState(turtleId: Long, trashedAt: Long?)

    @Insert
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long

    @Insert
    suspend fun insertLifeEvent(event: LifeEventEntity): Long

    @Insert
    suspend fun insertPhoto(photo: PhotoEntity): Long

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
}
