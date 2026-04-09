package de.leohopper.myturtle.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class CameraCaptureTarget(
    val outputUri: Uri,
    val cleanupPath: String,
)

data class RestorePhotoTarget(
    val file: File,
    val uriString: String,
)

class TurtleMediaStore(
    private val context: Context,
) {

    fun createCameraCaptureTarget(): CameraCaptureTarget {
        val directory = File(context.cacheDir, "camera_captures").apply {
            mkdirs()
        }
        val file = File(directory, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return CameraCaptureTarget(
            outputUri = uri,
            cleanupPath = file.absolutePath,
        )
    }

    suspend fun importMeasurementPhoto(sourceUri: Uri): String {
        return importPhoto(sourceUri = sourceUri, folder = "measurement_photos")
    }

    suspend fun importYearPhoto(sourceUri: Uri): String {
        return importPhoto(sourceUri = sourceUri, folder = "year_photos")
    }

    fun discardImportedPhoto(uriString: String?) {
        if (uriString.isNullOrBlank()) return

        val uri = Uri.parse(uriString)
        if (uri.scheme != "file") return

        val path = uri.path ?: return
        deleteIfInsideRoot(File(path), context.filesDir)
    }

    fun cleanupTemporaryCapture(cleanupPath: String?) {
        if (cleanupPath.isNullOrBlank()) return
        deleteIfInsideRoot(File(cleanupPath), context.cacheDir)
    }

    fun resolveImportedRelativePath(uriString: String?): String? {
        val photoFile = resolveImportedPhotoFile(uriString) ?: return null
        val canonicalRoot = importedMediaRoot().canonicalFile
        val canonicalFile = photoFile.canonicalFile
        val rootPath = canonicalRoot.path

        if (!canonicalFile.path.startsWith(rootPath)) return null

        return canonicalFile.path
            .removePrefix(rootPath)
            .trimStart(File.separatorChar)
            .replace(File.separatorChar, '/')
            .takeIf { it.isNotBlank() }
    }

    fun resolveImportedPhotoFile(uriString: String?): File? {
        if (uriString.isNullOrBlank()) return null

        val uri = Uri.parse(uriString)
        if (uri.scheme != "file") return null

        val file = uri.path?.let(::File) ?: return null
        return try {
            val canonicalFile = file.canonicalFile
            val canonicalRoot = importedMediaRoot().canonicalFile
            if (canonicalFile.path.startsWith(canonicalRoot.path)) canonicalFile else null
        } catch (_: IOException) {
            null
        }
    }

    fun createRestoreTarget(
        restoreSessionId: String,
        backupRelativePath: String,
    ): RestorePhotoTarget {
        val sanitizedPath = sanitizeRelativePath(backupRelativePath)
        val relativeImportedPath = "restored/$restoreSessionId/$sanitizedPath"
        val targetFile = File(importedMediaRoot(), relativeImportedPath.toPlatformPath())
        return RestorePhotoTarget(
            file = targetFile,
            uriString = Uri.fromFile(targetFile).toString(),
        )
    }

    fun pruneImportedMedia(keepUriStrings: Set<String>) {
        val keepFiles = keepUriStrings.mapNotNull { resolveImportedPhotoFile(it) }
            .mapNotNull {
                try {
                    it.canonicalFile.path
                } catch (_: IOException) {
                    null
                }
            }
            .toSet()

        val root = importedMediaRoot()
        if (!root.exists()) return

        root.walkBottomUp().forEach { file ->
            val canonicalPath = runCatching { file.canonicalFile.path }.getOrNull() ?: return@forEach
            when {
                file.isFile && canonicalPath !in keepFiles -> file.delete()
                file.isDirectory && file != root && file.listFiles().isNullOrEmpty() -> file.delete()
            }
        }
    }

    private suspend fun importPhoto(
        sourceUri: Uri,
        folder: String,
    ): String = withContext(Dispatchers.IO) {
        val orientation = readExifOrientation(sourceUri)
        val bitmap = decodeScaledBitmap(sourceUri)
            ?: throw IOException("Bild konnte nicht gelesen werden.")
        val orientedBitmap = applyOrientation(bitmap, orientation)
        val sanitizedBitmap = ensureJpegCompatible(orientedBitmap)

        val outputDir = File(context.filesDir, "imported_media/$folder").apply {
            mkdirs()
        }
        val outputFile = File(outputDir, "${folder}_${System.currentTimeMillis()}.jpg")

        FileOutputStream(outputFile).use { output ->
            if (!sanitizedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) {
                throw IOException("Bild konnte nicht gespeichert werden.")
            }
        }

        if (sanitizedBitmap !== orientedBitmap) sanitizedBitmap.recycle()
        if (orientedBitmap !== bitmap) orientedBitmap.recycle()
        bitmap.recycle()

        return@withContext Uri.fromFile(outputFile).toString()
    }

    private fun decodeScaledBitmap(sourceUri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(sourceUri).use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        val sampleSize = calculateSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            maxDimension = 2048,
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return context.contentResolver.openInputStream(sourceUri).use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        }
    }

    private fun calculateSampleSize(
        width: Int,
        height: Int,
        maxDimension: Int,
    ): Int {
        var sampleSize = 1
        var safeWidth = width
        var safeHeight = height

        while (safeWidth > maxDimension || safeHeight > maxDimension) {
            safeWidth /= 2
            safeHeight /= 2
            sampleSize *= 2
        }

        return sampleSize.coerceAtLeast(1)
    }

    private fun readExifOrientation(sourceUri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: IOException) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun applyOrientation(
        bitmap: Bitmap,
        orientation: Int,
    ): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(270f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(90f)
            }
            else -> return bitmap
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true,
        )
    }

    private fun ensureJpegCompatible(bitmap: Bitmap): Bitmap {
        if (!bitmap.hasAlpha()) return bitmap

        val flattenedBitmap = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.RGB_565,
        )
        Canvas(flattenedBitmap).apply {
            drawColor(Color.WHITE)
            drawBitmap(bitmap, 0f, 0f, null)
        }
        return flattenedBitmap
    }

    private fun deleteIfInsideRoot(
        file: File,
        root: File,
    ) {
        try {
            val canonicalFile = file.canonicalFile
            val canonicalRoot = root.canonicalFile
            if (canonicalFile.path.startsWith(canonicalRoot.path)) {
                canonicalFile.delete()
            }
        } catch (_: IOException) {
        }
    }

    private fun importedMediaRoot(): File = File(context.filesDir, "imported_media")

    private fun sanitizeRelativePath(path: String): String {
        val normalized = path.replace('\\', '/').trim('/')
        require(normalized.isNotBlank() && !normalized.contains("..")) {
            "Ungültiger Medienpfad im Backup."
        }
        return normalized
    }

    private fun String.toPlatformPath(): String = replace('/', File.separatorChar)
}
