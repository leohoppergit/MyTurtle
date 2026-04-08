package de.leohopper.myturtle.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.leohopper.myturtle.data.local.AppDatabase
import java.util.concurrent.TimeUnit

class TrashCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = TurtleRepository(
                turtleDao = AppDatabase.create(applicationContext).turtleDao(),
                mediaStore = TurtleMediaStore(applicationContext),
            )
            repository.purgeExpiredTrash()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

object TrashCleanupScheduler {
    private const val UNIQUE_WORK_NAME = "trash_cleanup"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<TrashCleanupWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
