package de.leohopper.myturtle.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.leohopper.myturtle.data.AppSettings
import de.leohopper.myturtle.data.CameraCaptureTarget
import de.leohopper.myturtle.data.HomeCardLayout
import de.leohopper.myturtle.data.TurtleInputParser
import de.leohopper.myturtle.data.TurtleRepository
import de.leohopper.myturtle.data.backup.TurtleBackupManager
import de.leohopper.myturtle.domain.HatchDateInfo
import de.leohopper.myturtle.domain.TurtleDetails
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyTurtleViewModel(
    private val repository: TurtleRepository,
    private val appSettings: AppSettings,
    private val backupManager: TurtleBackupManager,
) : ViewModel() {

    val turtles: StateFlow<List<TurtleDetails>> = repository.observeTurtles().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val trashedTurtles: StateFlow<List<TurtleDetails>> = repository.observeTrashedTurtles().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val homeCardLayout: StateFlow<HomeCardLayout> = appSettings.homeCardLayout

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()
    private val _isBackupOperationRunning = MutableStateFlow(false)
    val isBackupOperationRunning = _isBackupOperationRunning.asStateFlow()

    init {
        purgeExpiredTrash()
    }

    fun addTurtle(
        name: String,
        species: String,
        hatchDateInput: String,
        sex: String,
        notes: String,
    ): String? {
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            return "Bitte gib einen Namen für die Schildkröte an."
        }

        val hatchDate = TurtleInputParser.parseOptionalHatchDate(hatchDateInput) ?: run {
            if (hatchDateInput.isNotBlank()) {
                return "Bitte gib das Schlupfdatum als TT-MM-JJJJ, MM-JJJJ oder MM/JJ ein."
            }
            null
        }
        if (hatchDate != null && TurtleInputParser.isFutureHatchDate(hatchDate)) {
            return "Das Schlupfdatum darf nicht in der Zukunft liegen."
        }

        viewModelScope.launch {
            repository.addTurtle(
                name = cleanName,
                species = species.trim().ifBlank { "Griechische Landschildkröte" },
                hatchDate = hatchDate,
                sex = sex.trim().ifBlank { "Unbekannt" },
                notes = notes.trim(),
            )
            showMessage("Schildkröte gespeichert.")
        }
        return null
    }

    fun updateTurtle(
        id: Long,
        name: String,
        species: String,
        hatchDateInput: String,
        sex: String,
        notes: String,
    ): String? {
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            return "Der Name darf nicht leer sein."
        }

        val hatchDate = TurtleInputParser.parseOptionalHatchDate(hatchDateInput) ?: run {
            if (hatchDateInput.isNotBlank()) {
                return "Bitte gib das Schlupfdatum als TT-MM-JJJJ, MM-JJJJ oder MM/JJ ein."
            }
            null
        }
        if (hatchDate != null && TurtleInputParser.isFutureHatchDate(hatchDate)) {
            return "Das Schlupfdatum darf nicht in der Zukunft liegen."
        }

        viewModelScope.launch {
            repository.updateTurtle(
                id = id,
                name = cleanName,
                species = species.trim().ifBlank { "Griechische Landschildkröte" },
                hatchDate = hatchDate,
                sex = sex.trim().ifBlank { "Unbekannt" },
                notes = notes.trim(),
            )
            showMessage("Profil aktualisiert.")
        }
        return null
    }

    fun addMeasurement(
        turtleId: Long,
        dateInput: String,
        weightInput: String,
        lengthInput: String,
        notes: String,
        photoUris: List<String>,
        onFinished: (String?) -> Unit = {},
    ): String? {
        val date = TurtleInputParser.parseOptionalDate(dateInput)
        if (date == null) {
            return "Bitte gib für die Messung ein gültiges Datum ein."
        }
        if (date.isAfter(LocalDate.now())) {
            return "Das Messdatum darf nicht in der Zukunft liegen."
        }

        val weight = TurtleInputParser.parseOptionalFloat(weightInput)
        val length = TurtleInputParser.parseOptionalFloat(lengthInput)

        if (weightInput.isNotBlank() && weight == null) {
            return "Gewicht bitte als Zahl eingeben, zum Beispiel 37 oder 37,5."
        }

        if (lengthInput.isNotBlank() && length == null) {
            return "Länge bitte als Zahl eingeben, zum Beispiel 85 oder 85,5."
        }

        if (weight == null && length == null) {
            return "Bitte mindestens Gewicht oder Länge erfassen."
        }

        val stablePhotoUris = photoUris.toList()

        viewModelScope.launch {
            try {
                repository.addMeasurement(
                    turtleId = turtleId,
                    date = date,
                    weightGrams = weight,
                    carapaceLengthMm = length,
                    notes = notes.trim(),
                    photoUris = stablePhotoUris,
                )
                showMessage("Messung hinzugefügt.")
                onFinished(null)
            } catch (_: Exception) {
                onFinished("Die Messung konnte nicht gespeichert werden.")
            }
        }
        return null
    }

    fun addLifeEvent(
        turtleId: Long,
        dateInput: String,
        title: String,
        notes: String,
    ): String? {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            return "Bitte gib dem Ereignis einen Titel."
        }

        val date = TurtleInputParser.parseOptionalDate(dateInput)
        if (date == null) {
            return "Bitte gib für das Ereignis ein gültiges Datum ein."
        }
        if (date.isAfter(LocalDate.now())) {
            return "Das Ereignisdatum darf nicht in der Zukunft liegen."
        }

        viewModelScope.launch {
            repository.addLifeEvent(
                turtleId = turtleId,
                date = date,
                title = cleanTitle,
                notes = notes.trim(),
            )
            showMessage("Ereignis hinzugefügt.")
        }
        return null
    }

    fun addPhoto(
        turtleId: Long,
        yearInput: String,
        dateInput: String,
        caption: String,
        contentUri: String,
    ): String? {
        if (contentUri.isBlank()) {
            return "Es wurde kein Foto übernommen."
        }

        val date = TurtleInputParser.parseOptionalDate(dateInput) ?: run {
            if (dateInput.isNotBlank()) {
                return "Bitte gib für das Foto ein gültiges Datum ein."
            }
            null
        }
        if (date != null && date.isAfter(LocalDate.now())) {
            return "Das Fotodatum darf nicht in der Zukunft liegen."
        }

        val year = when {
            yearInput.isBlank() && date != null -> date.year
            yearInput.isBlank() -> LocalDate.now().year
            else -> yearInput.toIntOrNull()
        }

        if (year == null || year !in 1900..2200) {
            return "Bitte gib für das Foto ein sinnvolles Jahr ein."
        }
        if (year > LocalDate.now().year) {
            return "Das Fotojahr darf nicht in der Zukunft liegen."
        }

        viewModelScope.launch {
            repository.addPhoto(
                turtleId = turtleId,
                year = year,
                date = date,
                caption = caption.trim(),
                contentUri = contentUri,
            )
            showMessage("Foto verknüpft.")
        }
        return null
    }

    fun createCameraCaptureTarget(): CameraCaptureTarget = repository.createCameraCaptureTarget()

    suspend fun importMeasurementPhoto(sourceUri: Uri): String? {
        return try {
            repository.importMeasurementPhoto(sourceUri)
        } catch (_: Exception) {
            showMessage("Das Foto konnte nicht importiert werden.")
            null
        }
    }

    suspend fun importYearPhoto(sourceUri: Uri): String? {
        return try {
            repository.importYearPhoto(sourceUri)
        } catch (_: Exception) {
            showMessage("Das Foto konnte nicht importiert werden.")
            null
        }
    }

    fun discardImportedPhoto(uriString: String?) {
        repository.discardImportedPhoto(uriString)
    }

    fun discardImportedPhotos(uriStrings: List<String>) {
        repository.discardImportedPhotos(uriStrings)
    }

    fun cleanupTemporaryCapture(cleanupPath: String?) {
        repository.cleanupTemporaryCapture(cleanupPath)
    }

    fun moveTurtleToTrash(turtleId: Long) {
        viewModelScope.launch {
            repository.moveTurtleToTrash(turtleId)
            showMessage("Schildkröte in den Papierkorb verschoben.")
        }
    }

    fun restoreTurtle(turtleId: Long) {
        viewModelScope.launch {
            repository.restoreTurtle(turtleId)
            showMessage("Schildkröte wiederhergestellt.")
        }
    }

    fun deleteTurtleFromTrash(turtleId: Long) {
        viewModelScope.launch {
            repository.deleteTurtleFromTrash(turtleId)
            showMessage("Schildkröte endgültig gelöscht.")
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            val deletedCount = repository.emptyTrash()
            showMessage(
                if (deletedCount > 0) {
                    "Papierkorb geleert."
                } else {
                    "Der Papierkorb ist bereits leer."
                },
            )
        }
    }

    fun deleteMeasurement(measurementId: Long) {
        viewModelScope.launch {
            repository.deleteMeasurement(measurementId)
            showMessage("Messung gelöscht.")
        }
    }

    fun deleteLifeEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteLifeEvent(eventId)
            showMessage("Ereignis gelöscht.")
        }
    }

    fun deletePhoto(photoId: Long) {
        viewModelScope.launch {
            repository.deletePhoto(photoId)
            showMessage("Fotoeintrag gelöscht.")
        }
    }

    fun purgeExpiredTrash() {
        viewModelScope.launch {
            repository.purgeExpiredTrash()
        }
    }

    fun updateHomeCardLayout(layout: HomeCardLayout) {
        appSettings.updateHomeCardLayout(layout)
    }

    fun exportBackup(targetUri: Uri) {
        if (_isBackupOperationRunning.value) return

        viewModelScope.launch {
            _isBackupOperationRunning.value = true
            try {
                val summary = backupManager.exportBackup(targetUri)
                showMessage(
                    "Backup exportiert: ${summary.turtleCount} Schildkröten, ${summary.measurementCount} Messungen, ${summary.photoCount} Fotos.",
                )
            } catch (_: Exception) {
                showMessage("Das Backup konnte nicht exportiert werden.")
            } finally {
                _isBackupOperationRunning.value = false
            }
        }
    }

    fun importBackup(sourceUri: Uri) {
        if (_isBackupOperationRunning.value) return

        viewModelScope.launch {
            _isBackupOperationRunning.value = true
            try {
                val summary = backupManager.importBackup(sourceUri)
                purgeExpiredTrash()
                showMessage(
                    "Backup wiederhergestellt: ${summary.turtleCount} Schildkröten, ${summary.measurementCount} Messungen, ${summary.photoCount} Fotos.",
                )
            } catch (_: Exception) {
                showMessage("Das Backup konnte nicht wiederhergestellt werden.")
            } finally {
                _isBackupOperationRunning.value = false
            }
        }
    }

    private fun showMessage(message: String) {
        _messages.tryEmit(message)
    }

    class Factory(
        private val repository: TurtleRepository,
        private val appSettings: AppSettings,
        private val backupManager: TurtleBackupManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MyTurtleViewModel(
                repository = repository,
                appSettings = appSettings,
                backupManager = backupManager,
            ) as T
        }
    }
}
