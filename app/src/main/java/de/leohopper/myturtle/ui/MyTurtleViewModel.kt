package de.leohopper.myturtle.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.leohopper.myturtle.data.CameraCaptureTarget
import de.leohopper.myturtle.data.TurtleRepository
import de.leohopper.myturtle.domain.HatchDateInfo
import de.leohopper.myturtle.domain.HatchDatePrecision
import de.leohopper.myturtle.domain.TurtleDetails
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyTurtleViewModel(
    private val repository: TurtleRepository,
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

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

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

        val hatchDate = parseOptionalHatchDate(hatchDateInput) ?: run {
            if (hatchDateInput.isNotBlank()) {
                return "Bitte gib das Schlupfdatum als TT-MM-JJJJ, MM-JJJJ oder MM/JJ ein."
            }
            null
        }
        if (hatchDate != null && isFutureHatchDate(hatchDate)) {
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

        val hatchDate = parseOptionalHatchDate(hatchDateInput) ?: run {
            if (hatchDateInput.isNotBlank()) {
                return "Bitte gib das Schlupfdatum als TT-MM-JJJJ, MM-JJJJ oder MM/JJ ein."
            }
            null
        }
        if (hatchDate != null && isFutureHatchDate(hatchDate)) {
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
        val date = parseOptionalDate(dateInput)
        if (date == null) {
            return "Bitte gib für die Messung ein gültiges Datum ein."
        }
        if (date.isAfter(LocalDate.now())) {
            return "Das Messdatum darf nicht in der Zukunft liegen."
        }

        val weight = parseOptionalFloat(weightInput)
        val length = parseOptionalFloat(lengthInput)

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

        val date = parseOptionalDate(dateInput)
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

        val date = parseOptionalDate(dateInput) ?: run {
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

    private fun showMessage(message: String) {
        _messages.tryEmit(message)
    }

    private fun parseOptionalFloat(text: String): Float? {
        val normalized = text.trim().replace(',', '.')
        return normalized.toFloatOrNull()?.takeIf { it > 0f }
    }

    private fun isFutureHatchDate(hatchDate: HatchDateInfo): Boolean {
        return when (hatchDate.precision) {
            HatchDatePrecision.DAY -> hatchDate.date.isAfter(LocalDate.now())
            HatchDatePrecision.MONTH -> YearMonth.from(hatchDate.date).isAfter(YearMonth.now())
        }
    }

    private fun parseOptionalDate(text: String): LocalDate? {
        val clean = text.trim()
        if (clean.isBlank()) return null

        return FULL_DATE_FORMATTERS.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(clean, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    private fun parseOptionalHatchDate(text: String): HatchDateInfo? {
        val clean = text.trim()
        if (clean.isBlank()) return null

        FULL_DATE_FORMATTERS.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(clean, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        }?.let { date ->
            return HatchDateInfo(
                date = date,
                precision = HatchDatePrecision.DAY,
            )
        }

        val monthYear = MONTH_YEAR_FORMATTERS.firstNotNullOfOrNull { formatter ->
            try {
                YearMonth.parse(clean, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        } ?: parseCompactMonthYear(clean)

        monthYear?.let { parsedMonthYear ->
            return HatchDateInfo(
                date = parsedMonthYear.atDay(1),
                precision = HatchDatePrecision.MONTH,
            )
        }

        return null
    }

    private fun parseCompactMonthYear(text: String): YearMonth? {
        val match = SHORT_MONTH_YEAR_REGEX.matchEntire(text) ?: return null
        val month = match.groupValues[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
        val shortYear = match.groupValues[2].toIntOrNull() ?: return null

        return runCatching {
            YearMonth.of(2000 + shortYear, month)
        }.getOrNull()
    }

    companion object {
        private val FULL_DATE_FORMATTERS = listOf(
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ISO_LOCAL_DATE,
        )

        private val MONTH_YEAR_FORMATTERS = listOf(
            DateTimeFormatter.ofPattern("MM-uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("MM.uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("MM/uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("M-uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("M.uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("M/uuuu").withResolverStyle(ResolverStyle.STRICT),
        )

        private val SHORT_MONTH_YEAR_REGEX = Regex("""^\s*(\d{1,2})\s*[/.-]\s*(\d{2})\s*$""")
    }

    class Factory(
        private val repository: TurtleRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MyTurtleViewModel(repository = repository) as T
        }
    }
}
