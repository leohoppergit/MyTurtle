package de.leohopper.myturtle

import android.app.Application
import de.leohopper.myturtle.data.AppSettings
import de.leohopper.myturtle.data.backup.TurtleBackupManager
import de.leohopper.myturtle.data.TurtleRepository
import de.leohopper.myturtle.data.TrashCleanupScheduler
import de.leohopper.myturtle.data.TurtleMediaStore
import de.leohopper.myturtle.data.local.AppDatabase

class MyTurtleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TrashCleanupScheduler.schedule(this)
    }

    val database: AppDatabase by lazy {
        AppDatabase.create(this)
    }

    val mediaStore: TurtleMediaStore by lazy {
        TurtleMediaStore(this)
    }

    val appSettings: AppSettings by lazy {
        AppSettings(this)
    }

    val repository: TurtleRepository by lazy {
        TurtleRepository(
            turtleDao = database.turtleDao(),
            mediaStore = mediaStore,
        )
    }

    val backupManager: TurtleBackupManager by lazy {
        TurtleBackupManager(
            context = this,
            turtleDao = database.turtleDao(),
            mediaStore = mediaStore,
            appSettings = appSettings,
        )
    }
}
