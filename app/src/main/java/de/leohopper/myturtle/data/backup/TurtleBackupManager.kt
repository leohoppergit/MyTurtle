package de.leohopper.myturtle.data.backup

import android.content.Context
import android.net.Uri
import de.leohopper.myturtle.data.AppSettings
import de.leohopper.myturtle.data.TurtleMediaStore
import de.leohopper.myturtle.data.local.LifeEventEntity
import de.leohopper.myturtle.data.local.MeasurementEntity
import de.leohopper.myturtle.data.local.PhotoEntity
import de.leohopper.myturtle.data.local.TurtleDao
import de.leohopper.myturtle.data.local.TurtleEntity
import de.leohopper.myturtle.data.local.TurtleWithDetails
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TurtleBackupManager(
    private val context: Context,
    private val turtleDao: TurtleDao,
    private val mediaStore: TurtleMediaStore,
    private val appSettings: AppSettings,
) {

    suspend fun exportBackup(targetUri: Uri): BackupSummary = withContext(Dispatchers.IO) {
        val turtles = turtleDao.getAllTurtlesForBackup()
        val mediaEntries = linkedMapOf<String, File>()
        val backupBundle = BackupBundle(
            appVersionName = resolveInstalledVersionName(),
            exportedAt = Instant.now().toString(),
            settings = BackupSettings(
                homeCardLayout = appSettings.currentHomeCardLayout().name,
            ),
            turtles = turtles.map { turtle ->
                turtle.toBackupTurtle { uriString ->
                    val relativePath = mediaStore.resolveImportedRelativePath(uriString)
                        ?: throw IOException("Ein Foto konnte im internen Speicher nicht gefunden werden.")
                    val photoFile = mediaStore.resolveImportedPhotoFile(uriString)
                        ?.takeIf { it.exists() }
                        ?: throw IOException("Ein Foto konnte im internen Speicher nicht gefunden werden.")
                    mediaEntries.putIfAbsent(relativePath, photoFile)
                    relativePath
                }
            },
        )

        context.contentResolver.openOutputStream(targetUri)?.use { rawOutput ->
            ZipOutputStream(BufferedOutputStream(rawOutput)).use { zipOutput ->
                writeZipEntry(
                    zipOutput = zipOutput,
                    entryName = MANIFEST_ENTRY,
                    bytes = BackupCodec.encode(backupBundle).encodeToByteArray(),
                )

                mediaEntries.forEach { (relativePath, file) ->
                    writeZipFile(
                        zipOutput = zipOutput,
                        entryName = "$MEDIA_DIR/$relativePath",
                        sourceFile = file,
                    )
                }
            }
        } ?: throw IOException("Die Backup-Datei konnte nicht geschrieben werden.")

        backupBundle.summary()
    }

    suspend fun importBackup(sourceUri: Uri): BackupSummary = withContext(Dispatchers.IO) {
        val stagingDir = File(context.cacheDir, "backup_restore/staging_${System.currentTimeMillis()}").apply {
            mkdirs()
        }

        try {
            extractZipToDirectory(sourceUri = sourceUri, targetDir = stagingDir)

            val manifestFile = File(stagingDir, MANIFEST_ENTRY)
            if (!manifestFile.exists()) {
                throw IOException("Die ausgewählte Datei enthält kein gültiges MyTurtle-Backup.")
            }

            val backupBundle = BackupCodec.decode(manifestFile.readText())
            backupBundle.requireSupported()

            val restoreSessionId = "restore_${System.currentTimeMillis()}"
            val stagedMediaRoot = File(stagingDir, MEDIA_DIR)
            val restoredMediaUris = linkedMapOf<String, String>()

            backupBundle.referencedMediaPaths().forEach { backupPath ->
                val stagedMediaFile = File(stagedMediaRoot, backupPath.toPlatformPath()).canonicalFile
                if (!stagedMediaFile.exists()) {
                    throw IOException("Ein Foto aus dem Backup konnte nicht wiederhergestellt werden.")
                }

                val target = mediaStore.createRestoreTarget(
                    restoreSessionId = restoreSessionId,
                    backupRelativePath = backupPath,
                )
                target.file.parentFile?.mkdirs()
                stagedMediaFile.inputStream().use { input ->
                    FileOutputStream(target.file).use { output ->
                        input.copyTo(output)
                    }
                }
                restoredMediaUris[backupPath] = target.uriString
            }

            val restoreData = backupBundle.toRestoreData { backupPath ->
                restoredMediaUris[backupPath]
                    ?: throw IOException("Ein Foto aus dem Backup konnte nicht zugeordnet werden.")
            }

            turtleDao.replaceAllData(
                turtles = restoreData.turtles,
                measurements = restoreData.measurements,
                lifeEvents = restoreData.lifeEvents,
                photos = restoreData.photos,
            )
            appSettings.updateHomeCardLayout(restoreData.homeCardLayout)
            mediaStore.pruneImportedMedia(restoreData.referencedPhotoUris)

            backupBundle.summary()
        } finally {
            stagingDir.deleteRecursively()
        }
    }

    private fun extractZipToDirectory(
        sourceUri: Uri,
        targetDir: File,
    ) {
        context.contentResolver.openInputStream(sourceUri)?.use { rawInput ->
            ZipInputStream(BufferedInputStream(rawInput)).use { zipInput ->
                var entry: ZipEntry? = zipInput.nextEntry
                while (entry != null) {
                    val sanitizedPath = sanitizeZipEntryName(entry.name)
                    val outputFile = File(targetDir, sanitizedPath)

                    if (entry.isDirectory) {
                        outputFile.mkdirs()
                    } else {
                        outputFile.parentFile?.mkdirs()
                        FileOutputStream(outputFile).use { output ->
                            zipInput.copyTo(output)
                        }
                    }
                    zipInput.closeEntry()
                    entry = zipInput.nextEntry
                }
            }
        } ?: throw IOException("Die Backup-Datei konnte nicht gelesen werden.")
    }

    private fun sanitizeZipEntryName(name: String): String {
        val normalized = name.replace('\\', '/').trimStart('/')
        require(normalized.isNotBlank() && !normalized.contains("..")) {
            "Die Backup-Datei enthält ungültige Dateipfade."
        }
        return normalized
    }

    private fun resolveInstalledVersionName(): String {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty().ifBlank { "unbekannt" }
    }

    private fun writeZipEntry(
        zipOutput: ZipOutputStream,
        entryName: String,
        bytes: ByteArray,
    ) {
        zipOutput.putNextEntry(ZipEntry(entryName))
        zipOutput.write(bytes)
        zipOutput.closeEntry()
    }

    private fun writeZipFile(
        zipOutput: ZipOutputStream,
        entryName: String,
        sourceFile: File,
    ) {
        zipOutput.putNextEntry(ZipEntry(entryName))
        sourceFile.inputStream().use { input ->
            input.copyTo(zipOutput)
        }
        zipOutput.closeEntry()
    }

    private fun BackupBundle.referencedMediaPaths(): Set<String> {
        return buildSet {
            turtles.forEach { turtle ->
                turtle.measurements.forEach { measurement ->
                    addAll(measurement.photoPaths)
                }
                turtle.photos.forEach { photo ->
                    add(photo.mediaPath)
                }
            }
        }
    }

    private fun TurtleWithDetails.toBackupTurtle(
        resolveMediaPath: (String) -> String,
    ): BackupTurtle {
        return BackupTurtle(
            id = turtle.id,
            name = turtle.name,
            species = turtle.species,
            hatchDate = turtle.hatchDate?.toString(),
            hatchDatePrecision = turtle.hatchDatePrecision,
            sex = turtle.sex,
            notes = turtle.notes,
            createdAt = turtle.createdAt,
            trashedAt = turtle.trashedAt,
            measurements = measurements.map { it.toBackupMeasurement(resolveMediaPath) },
            lifeEvents = lifeEvents.map { it.toBackupLifeEvent() },
            photos = photos.map { it.toBackupPhoto(resolveMediaPath) },
        )
    }

    private fun MeasurementEntity.toBackupMeasurement(
        resolveMediaPath: (String) -> String,
    ): BackupMeasurement {
        return BackupMeasurement(
            id = id,
            date = date.toString(),
            weightGrams = weightGrams,
            carapaceLengthMm = carapaceLengthMm,
            notes = notes,
            photoPaths = photoUris.map(resolveMediaPath),
        )
    }

    private fun LifeEventEntity.toBackupLifeEvent(): BackupLifeEvent {
        return BackupLifeEvent(
            id = id,
            date = date.toString(),
            title = title,
            category = category,
            notes = notes,
        )
    }

    private fun PhotoEntity.toBackupPhoto(
        resolveMediaPath: (String) -> String,
    ): BackupPhoto {
        return BackupPhoto(
            id = id,
            year = year,
            date = date?.toString(),
            caption = caption,
            mediaPath = resolveMediaPath(contentUri),
            addedAt = addedAt,
        )
    }

    private fun String.toPlatformPath(): String = replace('/', File.separatorChar)

    private companion object {
        const val MANIFEST_ENTRY = "manifest.json"
        const val MEDIA_DIR = "media"
    }
}
