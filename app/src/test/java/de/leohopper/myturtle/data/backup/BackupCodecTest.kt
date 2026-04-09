package de.leohopper.myturtle.data.backup

import de.leohopper.myturtle.data.HomeCardLayout
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {

    @Test
    fun `backup bundle roundtrip keeps nested data`() {
        val bundle = BackupBundle(
            appVersionName = "0.1.0-beta",
            exportedAt = "2026-04-09T10:15:30Z",
            settings = BackupSettings(
                homeCardLayout = HomeCardLayout.LARGE.name,
            ),
            turtles = listOf(
                BackupTurtle(
                    id = 1L,
                    name = "Flash",
                    species = "Griechische Landschildkröte",
                    hatchDate = "2022-08-01",
                    hatchDatePrecision = "MONTH",
                    sex = "Unbekannt",
                    notes = "Testnotiz",
                    createdAt = 123L,
                    measurements = listOf(
                        BackupMeasurement(
                            id = 11L,
                            date = "2026-04-09",
                            weightGrams = 37f,
                            photoPaths = listOf("measurement_photos/photo_1.jpg"),
                        ),
                    ),
                    lifeEvents = listOf(
                        BackupLifeEvent(
                            id = 21L,
                            date = "2026-04-01",
                            title = "Winterruhe beendet",
                        ),
                    ),
                    photos = listOf(
                        BackupPhoto(
                            id = 31L,
                            year = 2026,
                            date = "2026-04-09",
                            caption = "Frühlingsfoto",
                            mediaPath = "year_photos/photo_2.jpg",
                            addedAt = 456L,
                        ),
                    ),
                ),
            ),
        )

        val decoded = BackupCodec.decode(BackupCodec.encode(bundle))

        assertEquals(bundle, decoded)
        assertEquals(1, decoded.summary().turtleCount)
        assertEquals(1, decoded.summary().photoCount)
    }

    @Test
    fun `restore data resolves imported photo uris`() {
        val bundle = BackupBundle(
            appVersionName = "0.1.0-beta",
            exportedAt = "2026-04-09T10:15:30Z",
            settings = BackupSettings(homeCardLayout = HomeCardLayout.STANDARD.name),
            turtles = listOf(
                BackupTurtle(
                    id = 1L,
                    name = "Flash",
                    species = "Griechische Landschildkröte",
                    createdAt = 1L,
                    measurements = listOf(
                        BackupMeasurement(
                            id = 10L,
                            date = "2026-04-09",
                            photoPaths = listOf("measurement_photos/photo_1.jpg"),
                        ),
                    ),
                    photos = listOf(
                        BackupPhoto(
                            id = 20L,
                            year = 2026,
                            mediaPath = "year_photos/photo_2.jpg",
                            addedAt = 2L,
                        ),
                    ),
                ),
            ),
        )

        val restoreData = bundle.toRestoreData { path -> "file:///restored/$path" }

        assertEquals(HomeCardLayout.STANDARD, restoreData.homeCardLayout)
        assertEquals(listOf("file:///restored/measurement_photos/photo_1.jpg"), restoreData.measurements.first().photoUris)
        assertEquals("file:///restored/year_photos/photo_2.jpg", restoreData.photos.first().contentUri)
        assertTrue(restoreData.referencedPhotoUris.contains("file:///restored/year_photos/photo_2.jpg"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unsupported backup version is rejected`() {
        BackupBundle(
            backupVersion = 99,
            appVersionName = "0.1.0-beta",
            exportedAt = LocalDate.of(2026, 4, 9).toString(),
            settings = BackupSettings(),
            turtles = emptyList(),
        ).requireSupported()
    }
}
