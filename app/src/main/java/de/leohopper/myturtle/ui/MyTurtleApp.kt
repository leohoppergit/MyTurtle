package de.leohopper.myturtle.ui

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import de.leohopper.myturtle.domain.HatchDateInfo
import de.leohopper.myturtle.domain.HatchDatePrecision
import de.leohopper.myturtle.domain.LifeEventRecord
import de.leohopper.myturtle.domain.MeasurementRecord
import de.leohopper.myturtle.domain.PhotoRecord
import de.leohopper.myturtle.domain.TurtleDetails
import de.leohopper.myturtle.ui.components.ChartPoint
import de.leohopper.myturtle.ui.components.MeasurementLineChart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit

private enum class MainScreen {
    HOME,
    TRASH,
}

private enum class ChartMetric {
    WEIGHT,
    LENGTH,
}

private enum class PhotoAttachmentTarget {
    MEASUREMENT,
    YEAR_PHOTO,
}

private data class DropdownGroup(
    val title: String,
    val options: List<String>,
)

private val TURTLE_SPECIES_GROUPS = listOf(
    DropdownGroup(
        title = "Mediterrane Landschildkröten",
        options = listOf(
            "Griechische Landschildkröte",
            "Maurische Landschildkröte",
            "Breitrandschildkröte",
            "Vierzehenschildkröte",
            "Ägyptische Landschildkröte",
            "Kleinasiatische Landschildkröte",
            "Marginata x Hermanni (Hybrid)",
        ),
    ),
    DropdownGroup(
        title = "Weitere Landschildkröten",
        options = listOf(
            "Russische Landschildkröte",
            "Spornschildkröte",
            "Leopardschildkröte",
            "Pantherschildkröte",
            "Indische Sternschildkröte",
            "Burmesische Sternschildkröte",
            "Strahlenschildkröte",
            "Aldabra-Riesenschildkröte",
            "Galapagos-Riesenschildkröte",
            "Rotfußschildkröte",
            "Gelbfußschildkröte",
            "Kohleschildkröte",
            "Forstens Landschildkröte",
            "Flachschildkröte",
        ),
    ),
    DropdownGroup(
        title = "Sumpfschildkröten",
        options = listOf(
            "Europäische Sumpfschildkröte",
        ),
    ),
    DropdownGroup(
        title = "Wasserschildkröten",
        options = listOf(
            "Klappschildkröte",
            "Moschusschildkröte",
            "Rotwangen-Schmuckschildkröte",
            "Gelbwangen-Schmuckschildkröte",
            "Hieroglyphen-Schmuckschildkröte",
            "Zierschildkröte",
            "Diamantschildkröte",
            "Weichschildkröte",
        ),
    ),
)

private val TURTLE_SPECIES_OPTIONS = TURTLE_SPECIES_GROUPS.flatMap { it.options }

private val TURTLE_SEX_OPTIONS = listOf(
    "Unbekannt",
    "Männlich",
    "Weiblich",
)

private sealed interface DeleteRequest {
    data class Turtle(val turtle: TurtleDetails) : DeleteRequest
    data class TrashedTurtle(val turtle: TurtleDetails) : DeleteRequest
    data class Measurement(val measurement: MeasurementRecord) : DeleteRequest
    data class Event(val event: LifeEventRecord) : DeleteRequest
    data class Photo(val photo: PhotoRecord) : DeleteRequest
    data object EmptyTrash : DeleteRequest
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTurtleApp(
    viewModel: MyTurtleViewModel,
) {
    val turtles by viewModel.turtles.collectAsStateWithLifecycle()
    val trashedTurtles by viewModel.trashedTurtles.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by rememberSaveable { mutableStateOf(MainScreen.HOME) }
    var selectedTurtleId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showAddTurtleDialog by rememberSaveable { mutableStateOf(false) }
    var showEditTurtleDialog by rememberSaveable { mutableStateOf(false) }
    var showMeasurementDialog by rememberSaveable { mutableStateOf(false) }
    var showEventDialog by rememberSaveable { mutableStateOf(false) }
    val pendingMeasurementPhotoUris = remember { mutableStateListOf<String>() }
    var pendingYearPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var photoSourceTarget by rememberSaveable { mutableStateOf<PhotoAttachmentTarget?>(null) }
    var pendingCameraTarget by rememberSaveable { mutableStateOf<PhotoAttachmentTarget?>(null) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraCleanupPath by rememberSaveable { mutableStateOf<String?>(null) }
    var previewPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteRequest by remember { mutableStateOf<DeleteRequest?>(null) }

    val selectedTurtle = turtles.firstOrNull { it.id == selectedTurtleId }

    fun clearPendingImportedPhoto(target: PhotoAttachmentTarget) {
        when (target) {
            PhotoAttachmentTarget.MEASUREMENT -> {
                viewModel.discardImportedPhotos(pendingMeasurementPhotoUris.toList())
                pendingMeasurementPhotoUris.clear()
            }
            PhotoAttachmentTarget.YEAR_PHOTO -> {
                viewModel.discardImportedPhoto(pendingYearPhotoUri)
                pendingYearPhotoUri = null
            }
        }
    }

    fun applyImportedPhoto(target: PhotoAttachmentTarget, importedUri: String) {
        when (target) {
            PhotoAttachmentTarget.MEASUREMENT -> {
                pendingMeasurementPhotoUris += importedUri
            }
            PhotoAttachmentTarget.YEAR_PHOTO -> {
                viewModel.discardImportedPhoto(pendingYearPhotoUri)
                pendingYearPhotoUri = importedUri
            }
        }
    }

    fun importPhotoIntoTarget(
        target: PhotoAttachmentTarget,
        sourceUri: Uri,
        cleanupPath: String? = null,
    ) {
        coroutineScope.launch {
            val importedUri = when (target) {
                PhotoAttachmentTarget.MEASUREMENT -> viewModel.importMeasurementPhoto(sourceUri)
                PhotoAttachmentTarget.YEAR_PHOTO -> viewModel.importYearPhoto(sourceUri)
            }
            viewModel.cleanupTemporaryCapture(cleanupPath)
            if (importedUri != null) {
                applyImportedPhoto(target, importedUri)
            }
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        val target = photoSourceTarget
        photoSourceTarget = null

        if (uri != null && target != null) {
            importPhotoIntoTarget(
                target = target,
                sourceUri = uri,
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val target = pendingCameraTarget
        val captureUri = pendingCameraUri?.let(Uri::parse)
        val cleanupPath = pendingCameraCleanupPath

        pendingCameraTarget = null
        pendingCameraUri = null
        pendingCameraCleanupPath = null

        if (success && target != null && captureUri != null) {
            importPhotoIntoTarget(
                target = target,
                sourceUri = captureUri,
                cleanupPath = cleanupPath,
            )
        } else {
            viewModel.cleanupTemporaryCapture(cleanupPath)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == MainScreen.TRASH) {
            viewModel.purgeExpiredTrash()
        }
    }

    LaunchedEffect(turtles, selectedTurtleId) {
        if (selectedTurtleId != null && selectedTurtle == null) {
            selectedTurtleId = null
        }
    }

    Scaffold(
        topBar = {
            if (selectedTurtle == null) {
                if (currentScreen == MainScreen.HOME) {
                    CenterAlignedTopAppBar(
                        title = { Text("MyTurtle") },
                        actions = {
                            IconButton(onClick = { currentScreen = MainScreen.TRASH }) {
                                Icon(
                                    imageVector = Icons.Filled.RestoreFromTrash,
                                    contentDescription = "Papierkorb öffnen",
                                )
                            }
                        },
                    )
                } else {
                    CenterAlignedTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { currentScreen = MainScreen.HOME }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Zurück",
                                )
                            }
                        },
                        title = { Text("Papierkorb") },
                        actions = {
                            if (trashedTurtles.isNotEmpty()) {
                                IconButton(onClick = { deleteRequest = DeleteRequest.EmptyTrash }) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteSweep,
                                        contentDescription = "Papierkorb leeren",
                                    )
                                }
                            }
                        },
                    )
                }
            } else {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { selectedTurtleId = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Zurück",
                            )
                        }
                    },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedTurtle.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = selectedTurtle.species,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditTurtleDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Profil bearbeiten")
                        }
                        IconButton(onClick = { deleteRequest = DeleteRequest.Turtle(selectedTurtle) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "In den Papierkorb verschieben")
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (selectedTurtle == null && currentScreen == MainScreen.HOME) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTurtleDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Neue Schildkröte") },
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            if (selectedTurtle == null) {
                if (currentScreen == MainScreen.HOME) {
                    HomeScreen(
                        turtles = turtles,
                        contentPadding = innerPadding,
                        onOpenTurtle = { selectedTurtleId = it },
                        onCreateTurtle = { showAddTurtleDialog = true },
                    )
                } else {
                    TrashScreen(
                        turtles = trashedTurtles,
                        contentPadding = innerPadding,
                        onRestoreTurtle = viewModel::restoreTurtle,
                        onDeleteTurtle = { deleteRequest = DeleteRequest.TrashedTurtle(it) },
                        onEmptyTrash = { deleteRequest = DeleteRequest.EmptyTrash },
                    )
                }
            } else {
                TurtleDetailScreen(
                    turtle = selectedTurtle,
                    contentPadding = innerPadding,
                    onAddMeasurement = { showMeasurementDialog = true },
                    onAddEvent = { showEventDialog = true },
                    onAddPhoto = { photoSourceTarget = PhotoAttachmentTarget.YEAR_PHOTO },
                    onOpenPhoto = { previewPhotoUri = it },
                    onDeleteMeasurement = { deleteRequest = DeleteRequest.Measurement(it) },
                    onDeleteEvent = { deleteRequest = DeleteRequest.Event(it) },
                    onDeletePhoto = { deleteRequest = DeleteRequest.Photo(it) },
                )
            }
        }
    }

    if (showAddTurtleDialog) {
        TurtleDialog(
            title = "Schildkröte anlegen",
            confirmLabel = "Speichern",
            initialName = "",
            initialSpecies = TURTLE_SPECIES_OPTIONS.first(),
            initialHatchDate = "",
            initialSex = TURTLE_SEX_OPTIONS.first(),
            initialNotes = "",
            onDismiss = { showAddTurtleDialog = false },
            onConfirm = { name, species, hatchDate, sex, notes ->
                viewModel.addTurtle(name, species, hatchDate, sex, notes).also { error ->
                    if (error == null) {
                        showAddTurtleDialog = false
                    }
                }
            },
        )
    }

    if (showEditTurtleDialog && selectedTurtle != null) {
        TurtleDialog(
            title = "Profil bearbeiten",
            confirmLabel = "Aktualisieren",
            initialName = selectedTurtle.name,
            initialSpecies = selectedTurtle.species,
            initialHatchDate = formatHatchDateForInput(selectedTurtle.hatchDate),
            initialSex = selectedTurtle.sex ?: TURTLE_SEX_OPTIONS.first(),
            initialNotes = selectedTurtle.notes,
            onDismiss = { showEditTurtleDialog = false },
            onConfirm = { name, species, hatchDate, sex, notes ->
                viewModel.updateTurtle(
                    id = selectedTurtle.id,
                    name = name,
                    species = species,
                    hatchDateInput = hatchDate,
                    sex = sex,
                    notes = notes,
                ).also { error ->
                    if (error == null) {
                        showEditTurtleDialog = false
                    }
                }
            },
        )
    }

    if (showMeasurementDialog && selectedTurtle != null) {
        MeasurementDialog(
            photoUris = pendingMeasurementPhotoUris,
            onPhotoClick = { previewPhotoUri = it },
            onAddPhoto = { photoSourceTarget = PhotoAttachmentTarget.MEASUREMENT },
            onRemovePhoto = { photoUri ->
                viewModel.discardImportedPhoto(photoUri)
                pendingMeasurementPhotoUris.remove(photoUri)
            },
            onDismiss = {
                clearPendingImportedPhoto(PhotoAttachmentTarget.MEASUREMENT)
                showMeasurementDialog = false
            },
            onConfirm = { date, weight, length, notes, photoUris, onFinished ->
                val validationError = viewModel.addMeasurement(
                    turtleId = selectedTurtle.id,
                    dateInput = date,
                    weightInput = weight,
                    lengthInput = length,
                    notes = notes,
                    photoUris = photoUris.toList(),
                    onFinished = { saveError ->
                        if (saveError == null) {
                            pendingMeasurementPhotoUris.clear()
                            showMeasurementDialog = false
                        }
                        onFinished(saveError)
                    },
                )

                if (validationError != null) {
                    onFinished(validationError)
                }
            },
        )
    }

    if (showEventDialog && selectedTurtle != null) {
        EventDialog(
            onDismiss = { showEventDialog = false },
            onConfirm = { date, title, notes ->
                viewModel.addLifeEvent(
                    turtleId = selectedTurtle.id,
                    dateInput = date,
                    title = title,
                    notes = notes,
                ).also { error ->
                    if (error == null) {
                        showEventDialog = false
                    }
                }
            },
        )
    }

    if (pendingYearPhotoUri != null && selectedTurtle != null) {
        PhotoDialog(
            uri = pendingYearPhotoUri.orEmpty(),
            onDismiss = {
                clearPendingImportedPhoto(PhotoAttachmentTarget.YEAR_PHOTO)
            },
            onConfirm = { year, date, caption ->
                viewModel.addPhoto(
                    turtleId = selectedTurtle.id,
                    yearInput = year,
                    dateInput = date,
                    caption = caption,
                    contentUri = pendingYearPhotoUri.orEmpty(),
                ).also { error ->
                    if (error == null) {
                        pendingYearPhotoUri = null
                    }
                }
            },
        )
    }

    photoSourceTarget?.let { target ->
        PhotoSourceDialog(
            onDismiss = { photoSourceTarget = null },
            onPickFromGallery = {
                photoSourceTarget = null
                galleryPicker.launch("image/*")
            },
            onTakePhoto = {
                val captureTarget = viewModel.createCameraCaptureTarget()
                pendingCameraTarget = target
                pendingCameraUri = captureTarget.outputUri.toString()
                pendingCameraCleanupPath = captureTarget.cleanupPath
                photoSourceTarget = null
                cameraLauncher.launch(captureTarget.outputUri)
            },
        )
    }

    previewPhotoUri?.let { uri ->
        PhotoPreviewDialog(
            uri = uri,
            onDismiss = { previewPhotoUri = null },
        )
    }

    deleteRequest?.let { request ->
        ConfirmDeleteDialog(
            title = when (request) {
                is DeleteRequest.Event -> "Ereignis löschen?"
                DeleteRequest.EmptyTrash -> "Papierkorb leeren?"
                is DeleteRequest.Measurement -> "Messung löschen?"
                is DeleteRequest.Photo -> "Fotoeintrag löschen?"
                is DeleteRequest.TrashedTurtle -> "Schildkröte endgültig löschen?"
                is DeleteRequest.Turtle -> "Schildkröte in den Papierkorb verschieben?"
            },
            text = when (request) {
                is DeleteRequest.Event -> "Der Eintrag wird aus der Lebenschronik entfernt."
                DeleteRequest.EmptyTrash -> "Alle Schildkröten im Papierkorb werden sofort und endgültig gelöscht."
                is DeleteRequest.Measurement -> "Die Messung wird aus Verlauf und Tabelle entfernt."
                is DeleteRequest.Photo -> "Nur der Fotoeintrag wird entfernt, nicht die Originaldatei."
                is DeleteRequest.TrashedTurtle -> "Alle Messungen, Ereignisse und Fotos dieser Schildkröte werden sofort und endgültig entfernt."
                is DeleteRequest.Turtle -> "Alle Messungen, Ereignisse und Fotos bleiben zunächst im Papierkorb und werden dort nach 30 Tagen automatisch gelöscht."
            },
            confirmLabel = when (request) {
                DeleteRequest.EmptyTrash -> "Leeren"
                is DeleteRequest.Turtle -> "Verschieben"
                is DeleteRequest.TrashedTurtle -> "Endgültig löschen"
                else -> "Löschen"
            },
            onDismiss = { deleteRequest = null },
            onConfirm = {
                when (request) {
                    is DeleteRequest.Event -> viewModel.deleteLifeEvent(request.event.id)
                    is DeleteRequest.Measurement -> viewModel.deleteMeasurement(request.measurement.id)
                    is DeleteRequest.Photo -> viewModel.deletePhoto(request.photo.id)
                    DeleteRequest.EmptyTrash -> viewModel.emptyTrash()
                    is DeleteRequest.TrashedTurtle -> viewModel.deleteTurtleFromTrash(request.turtle.id)
                    is DeleteRequest.Turtle -> {
                        viewModel.moveTurtleToTrash(request.turtle.id)
                        selectedTurtleId = null
                        currentScreen = MainScreen.HOME
                    }
                }
                deleteRequest = null
            },
        )
    }
}

@Composable
private fun HomeScreen(
    turtles: List<TurtleDetails>,
    contentPadding: PaddingValues,
    onOpenTurtle: (Long) -> Unit,
    onCreateTurtle: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Datenschutzfreundliche Schildkröten-Dokumentation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Alle Daten bleiben lokal auf deinem Gerät. Du kannst Gewicht, Länge, Fotos und Lebensereignisse über viele Jahre hinweg dokumentieren.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        ElevatedCard {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Für die Grundversion vorgesehen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Gewichtsverlauf wie in deiner Tabelle, wiederkehrende Längenmessungen, Jahresfotos und freie Lebensereignisse.")
                Text("Die App ist bewusst klein gehalten: keine Cloud, kein Konto, keine Tracking-SDKs.")
            }
        }

        if (turtles.isEmpty()) {
            ElevatedCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Noch keine Schildkröte angelegt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("Lege zuerst ein Tier an. Danach kannst du Messwerte, Fotos und Lebensereignisse erfassen.")
                    TextButton(onClick = onCreateTurtle) {
                        Text("Erste Schildkröte anlegen")
                    }
                }
            }
        } else {
            turtles.forEach { turtle ->
                TurtleCard(
                    turtle = turtle,
                    onClick = { onOpenTurtle(turtle.id) },
                )
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun TrashScreen(
    turtles: List<TurtleDetails>,
    contentPadding: PaddingValues,
    onRestoreTurtle: (Long) -> Unit,
    onDeleteTurtle: (TurtleDetails) -> Unit,
    onEmptyTrash: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Gelöschte Schildkröten bleiben 30 Tage erhalten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Du kannst Tiere aus dem Papierkorb wiederherstellen oder den Papierkorb jederzeit manuell leeren.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (turtles.isNotEmpty()) {
                    TextButton(onClick = onEmptyTrash) {
                        Text("Papierkorb jetzt leeren")
                    }
                }
            }
        }

        if (turtles.isEmpty()) {
            ElevatedCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Papierkorb ist leer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("Gelöschte Schildkröten erscheinen hier für 30 Tage, bevor sie automatisch entfernt werden.")
                }
            }
        } else {
            turtles.forEach { turtle ->
                TrashTurtleCard(
                    turtle = turtle,
                    onRestore = { onRestoreTurtle(turtle.id) },
                    onDelete = { onDeleteTurtle(turtle) },
                )
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun TrashTurtleCard(
    turtle: TurtleDetails,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = turtle.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = turtle.species,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = trashStatusLabel(turtle.trashedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatChip(
                    icon = Icons.Filled.WaterDrop,
                    label = "${turtle.measurements.size} Messungen",
                )
                StatChip(
                    icon = Icons.Filled.Event,
                    label = "${turtle.lifeEvents.size} Ereignisse",
                )
                StatChip(
                    icon = Icons.Filled.Collections,
                    label = "${turtle.photos.size} Fotos",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onRestore) {
                    Text("Wiederherstellen")
                }
                TextButton(onClick = onDelete) {
                    Text("Endgültig löschen")
                }
            }
        }
    }
}

@Composable
private fun TurtleCard(
    turtle: TurtleDetails,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = turtle.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = turtle.species,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatChip(
                    icon = Icons.Filled.WaterDrop,
                    label = latestWeightLabel(turtle),
                )
                StatChip(
                    icon = Icons.Filled.Straighten,
                    label = latestLengthLabel(turtle),
                )
                StatChip(
                    icon = Icons.Filled.Event,
                    label = hatchDateLabel(turtle.hatchDate),
                )
            }
        }
    }
}

@Composable
private fun TurtleDetailScreen(
    turtle: TurtleDetails,
    contentPadding: PaddingValues,
    onAddMeasurement: () -> Unit,
    onAddEvent: () -> Unit,
    onAddPhoto: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onDeleteMeasurement: (MeasurementRecord) -> Unit,
    onDeleteEvent: (LifeEventRecord) -> Unit,
    onDeletePhoto: (PhotoRecord) -> Unit,
) {
    val scrollState = rememberScrollState()
    var chartMetric by rememberSaveable(turtle.id) { mutableStateOf(ChartMetric.WEIGHT) }

    val measurementsDescending = remember(turtle.measurements) {
        turtle.measurements.sortedByDescending { it.date }
    }
    val eventsDescending = remember(turtle.lifeEvents) {
        turtle.lifeEvents.sortedByDescending { it.date }
    }
    val photosByYear = remember(turtle.photos) {
        turtle.photos.sortedWith(compareByDescending<PhotoRecord> { it.year }.thenByDescending { it.date ?: LocalDate.MIN })
            .groupBy { it.year }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = turtle.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = ageLabel(turtle.hatchDate),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatChip(
                        icon = Icons.Filled.WaterDrop,
                        label = latestWeightLabel(turtle),
                    )
                    StatChip(
                        icon = Icons.Filled.Straighten,
                        label = latestLengthLabel(turtle),
                    )
                    StatChip(
                        icon = Icons.Filled.Favorite,
                        label = turtle.sex ?: TURTLE_SEX_OPTIONS.first(),
                    )
                }
                if (turtle.notes.isNotBlank()) {
                    HorizontalDivider()
                    Text(
                        text = turtle.notes,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        SectionCard(
            title = "Verlauf",
            subtitle = "Gewicht oder Panzerlänge übersichtlich über die Zeit",
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = chartMetric == ChartMetric.WEIGHT,
                    onClick = { chartMetric = ChartMetric.WEIGHT },
                    label = { Text("Gewicht") },
                    leadingIcon = {
                        Icon(Icons.Filled.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                )
                FilterChip(
                    selected = chartMetric == ChartMetric.LENGTH,
                    onClick = { chartMetric = ChartMetric.LENGTH },
                    label = { Text("Länge") },
                    leadingIcon = {
                        Icon(Icons.Filled.Straighten, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                )
            }

            val points = when (chartMetric) {
                ChartMetric.WEIGHT -> turtle.measurements.mapNotNull { measurement ->
                    measurement.weightGrams?.let {
                        ChartPoint(label = measurement.date.format(SHORT_DATE_FORMAT), value = it)
                    }
                }
                ChartMetric.LENGTH -> turtle.measurements.mapNotNull { measurement ->
                    measurement.carapaceLengthMm?.let {
                        ChartPoint(label = measurement.date.format(SHORT_DATE_FORMAT), value = it)
                    }
                }
            }

            MeasurementLineChart(
                points = points,
                emptyLabel = when (chartMetric) {
                    ChartMetric.WEIGHT -> "Noch zu wenige Gewichtswerte für eine Kurve."
                    ChartMetric.LENGTH -> "Noch zu wenige Längenwerte für eine Kurve."
                },
            )

            if (points.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = points.first().label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = points.last().label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AssistChip(
                onClick = onAddMeasurement,
                label = { Text("Neue Messung") },
                leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        }

        SectionCard(
            title = "Messungen",
            subtitle = "Digitales Gegenstück zu deiner bisherigen Tabelle",
            action = {
                TextButton(onClick = onAddMeasurement) {
                    Text("Hinzufügen")
                }
            },
        ) {
            if (measurementsDescending.isEmpty()) {
                EmptySectionText("Noch keine Messwerte vorhanden.")
            } else {
                measurementsDescending.forEachIndexed { index, measurement ->
                    MeasurementRow(
                        measurement = measurement,
                        onPhotoClick = onOpenPhoto,
                        onDelete = { onDeleteMeasurement(measurement) },
                    )
                    if (index != measurementsDescending.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        SectionCard(
            title = "Lebensereignisse",
            subtitle = "Zum Beispiel Winterruhe, Tierarzt, erstes Gelege oder Besonderheiten",
            action = {
                TextButton(onClick = onAddEvent) {
                    Text("Hinzufügen")
                }
            },
        ) {
            if (eventsDescending.isEmpty()) {
                EmptySectionText("Noch keine Lebensereignisse erfasst.")
            } else {
                eventsDescending.forEachIndexed { index, event ->
                    EventRow(
                        event = event,
                        onDelete = { onDeleteEvent(event) },
                    )
                    if (index != eventsDescending.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        SectionCard(
            title = "Jahresfotos",
            subtitle = "Fotos werden lokal aus Kamera oder Galerie importiert",
            action = {
                TextButton(onClick = onAddPhoto) {
                    Text("Foto hinzufügen")
                }
            },
        ) {
            if (photosByYear.isEmpty()) {
                EmptySectionText("Noch keine Fotos verknüpft.")
            } else {
                photosByYear.forEach { (year, photos) ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            photos.forEach { photo ->
                                PhotoCard(
                                    photo = photo,
                                    onOpen = { onOpenPhoto(photo.contentUri) },
                                    onDelete = { onDeletePhoto(photo) },
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    action?.invoke()
                }
                content()
            },
        )
    }
}

@Composable
private fun MeasurementRow(
    measurement: MeasurementRecord,
    onPhotoClick: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = measurement.date.format(UI_DATE_FORMAT),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            val values = buildList {
                measurement.weightGrams?.let { add("${formatDecimal(it)} g") }
                measurement.carapaceLengthMm?.let { add("${formatDecimal(it)} mm") }
            }
            Text(
                text = values.joinToString(" | "),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (measurement.notes.isNotBlank()) {
                Text(
                    text = measurement.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (measurement.photoUris.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    measurement.photoUris.forEach { photoUri ->
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Messungsfoto",
                            modifier = Modifier
                                .width(120.dp)
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onPhotoClick(photoUri) },
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Messung löschen")
        }
    }
}

@Composable
private fun EventRow(
    event: LifeEventRecord,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = eventMetaLabel(event),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (event.notes.isNotBlank()) {
                Text(
                    text = event.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Ereignis löschen")
        }
    }
}

@Composable
private fun PhotoCard(
    photo: PhotoRecord,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.width(180.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AsyncImage(
                model = photo.contentUri,
                contentDescription = photo.caption.ifBlank { "Jahresfoto" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable(onClick = onOpen),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = photo.date?.format(UI_DATE_FORMAT) ?: "${photo.year}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = photo.caption.ifBlank { "Ohne Beschreibung" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Fotoeintrag löschen")
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
    )
}

@Composable
private fun EmptySectionText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ValidationErrorBanner(
    message: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    allowManualInput: Boolean = true,
) {
    val context = LocalContext.current
    val openPicker = {
        val initialDate = parseDateInputOrToday(value)
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onValueChange(
                    LocalDate.of(year, month + 1, dayOfMonth)
                        .format(INPUT_DATE_FORMAT),
                )
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth,
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (allowManualInput) {
                onValueChange(it)
            }
        },
        readOnly = !allowManualInput,
        label = { Text(label) },
        supportingText = { Text(supportingText) },
        trailingIcon = {
            IconButton(
                onClick = openPicker,
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = "Datum auswählen",
                )
            }
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (allowManualInput) {
                    Modifier
                } else {
                    Modifier.clickable(onClick = openPicker)
                }
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelectionField(
    label: String,
    value: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    groups: List<DropdownGroup>? = null,
) {
    var expanded by rememberSaveable(label) { mutableStateOf(false) }
    val groupedOptions = groups ?: listOf(DropdownGroup(title = "", options = options))

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            groupedOptions.forEachIndexed { index, group ->
                if (group.title.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                group.options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        },
                    )
                }
                if (index != groupedOptions.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun SelectedPhotoPreview(
    photoUri: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = photoUri,
        contentDescription = "Ausgewähltes Foto",
        modifier = modifier
            .fillMaxWidth()
            .height(164.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun PhotoPreviewDialog(
    uri: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fotoansicht") },
        text = {
            SelectedPhotoPreview(
                photoUri = uri,
                modifier = Modifier.height(360.dp),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        },
    )
}

@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foto hinzufügen") },
        text = { Text("Wie möchtest du das Foto in die App übernehmen? EXIF-Daten werden beim Import entfernt.") },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onPickFromGallery) {
                    Icon(Icons.Filled.Collections, contentDescription = null)
                    Text("Galerie")
                }
                TextButton(onClick = onTakePhoto) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Text("Kamera")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
    )
}

@Composable
private fun TurtleDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    initialSpecies: String,
    initialHatchDate: String,
    initialSex: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> String?,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var species by rememberSaveable {
        mutableStateOf(
            initialSpecies.takeIf { it in TURTLE_SPECIES_OPTIONS } ?: TURTLE_SPECIES_OPTIONS.first()
        )
    }
    var hatchDate by rememberSaveable { mutableStateOf(initialHatchDate) }
    var sex by rememberSaveable { mutableStateOf(initialSex.ifBlank { TURTLE_SEX_OPTIONS.first() }) }
    var notes by rememberSaveable { mutableStateOf(initialNotes) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMessage?.let { ValidationErrorBanner(it) }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownSelectionField(
                    label = "Art",
                    value = species,
                    options = TURTLE_SPECIES_OPTIONS,
                    onValueSelected = {
                        species = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    groups = TURTLE_SPECIES_GROUPS,
                )
                DateInputField(
                    value = hatchDate,
                    onValueChange = {
                        hatchDate = it
                        errorMessage = null
                    },
                    label = "Schlupfdatum",
                    supportingText = "TT-MM-JJJJ, MM-JJJJ oder MM/JJ",
                    modifier = Modifier.fillMaxWidth(),
                    allowManualInput = true,
                )
                DropdownSelectionField(
                    label = "Geschlecht",
                    value = sex,
                    options = TURTLE_SEX_OPTIONS,
                    onValueSelected = {
                        sex = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        errorMessage = null
                    },
                    label = { Text("Notizen") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { errorMessage = onConfirm(name, species, hatchDate, sex, notes) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
    )
}

@Composable
private fun MeasurementDialog(
    photoUris: List<String>,
    onPhotoClick: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, List<String>, (String?) -> Unit) -> Unit,
) {
    var date by rememberSaveable { mutableStateOf(LocalDate.now().format(INPUT_DATE_FORMAT)) }
    var weight by rememberSaveable { mutableStateOf("") }
    var length by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = { Text("Messung hinzufügen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMessage?.let { ValidationErrorBanner(it) }
                DateInputField(
                    value = date,
                    onValueChange = {
                        date = it
                        errorMessage = null
                    },
                    label = "Datum",
                    supportingText = "Tippe auf das Feld oder das Kalendersymbol",
                    modifier = Modifier.fillMaxWidth(),
                    allowManualInput = false,
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        weight = it
                        errorMessage = null
                    },
                    label = { Text("Gewicht in g") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = length,
                    onValueChange = {
                        length = it
                        errorMessage = null
                    },
                    label = { Text("Panzerlänge in mm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        errorMessage = null
                    },
                    label = { Text("Notizen") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Messungsfoto",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (photoUris.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        photoUris.forEach { photoUri ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                SelectedPhotoPreview(
                                    photoUri = photoUri,
                                    onClick = { onPhotoClick(photoUri) },
                                    modifier = Modifier.width(160.dp),
                                )
                                TextButton(onClick = {
                                    errorMessage = null
                                    onRemovePhoto(photoUri)
                                }) {
                                    Text("Entfernen")
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        enabled = !isSaving,
                        onClick = {
                            errorMessage = null
                            onAddPhoto()
                        },
                    ) {
                        Text(if (photoUris.isEmpty()) "Foto hinzufügen" else "Weiteres Foto hinzufügen")
                    }
                }
                Text(
                    text = "Aufgenommene oder importierte Bilder werden vor dem Speichern ohne EXIF-Daten übernommen. Mehrere Fotos pro Messung sind möglich.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    errorMessage = null
                    isSaving = true
                    onConfirm(date, weight, length, notes, photoUris) { saveError ->
                        isSaving = false
                        errorMessage = saveError
                    }
                },
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss,
            ) {
                Text("Abbrechen")
            }
        },
    )
}

@Composable
private fun EventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> String?,
) {
    var date by rememberSaveable { mutableStateOf(LocalDate.now().format(INPUT_DATE_FORMAT)) }
    var title by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ereignis hinzufügen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMessage?.let { ValidationErrorBanner(it) }
                DateInputField(
                    value = date,
                    onValueChange = {
                        date = it
                        errorMessage = null
                    },
                    label = "Datum",
                    supportingText = "Tippe auf das Feld oder das Kalendersymbol",
                    modifier = Modifier.fillMaxWidth(),
                    allowManualInput = false,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Titel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        errorMessage = null
                    },
                    label = { Text("Beschreibung") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { errorMessage = onConfirm(date, title, notes) }) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
    )
}

@Composable
private fun PhotoDialog(
    uri: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> String?,
) {
    var year by rememberSaveable { mutableStateOf(LocalDate.now().year.toString()) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().format(INPUT_DATE_FORMAT)) }
    var caption by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foto verknüpfen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMessage?.let { ValidationErrorBanner(it) }
                AsyncImage(
                    model = uri,
                    contentDescription = "Ausgewähltes Foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        year = it
                        errorMessage = null
                    },
                    label = { Text("Jahr") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                DateInputField(
                    value = date,
                    onValueChange = {
                        date = it
                        year = parseDateInputOrToday(it).year.toString()
                        errorMessage = null
                    },
                    label = "Aufnahmedatum",
                    supportingText = "Tippe auf das Feld oder das Kalendersymbol",
                    modifier = Modifier.fillMaxWidth(),
                    allowManualInput = false,
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = {
                        caption = it
                        errorMessage = null
                    },
                    label = { Text("Bildbeschreibung") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Beim Import werden EXIF-Daten des Bildes entfernt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { errorMessage = onConfirm(year, date, caption) }) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
    )
}

private fun parseDateInputOrToday(text: String): LocalDate {
    val clean = text.trim()
    if (clean.isBlank()) return LocalDate.now()

    FULL_DATE_INPUT_FORMATTERS.firstNotNullOfOrNull { formatter ->
        try {
            LocalDate.parse(clean, formatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }?.let { return it }

    MONTH_YEAR_INPUT_FORMATTERS.firstNotNullOfOrNull { formatter ->
        try {
            YearMonth.parse(clean, formatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }?.let { return it.atDay(1) }

    return parseCompactMonthYear(clean)?.atDay(1) ?: LocalDate.now()
}

private fun parseCompactMonthYear(text: String): YearMonth? {
    val match = SHORT_MONTH_YEAR_REGEX.matchEntire(text) ?: return null
    val month = match.groupValues[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
    val shortYear = match.groupValues[2].toIntOrNull() ?: return null

    return runCatching {
        YearMonth.of(2000 + shortYear, month)
    }.getOrNull()
}

private fun latestWeightLabel(turtle: TurtleDetails): String {
    val latest = turtle.measurements.lastOrNull { it.weightGrams != null }?.weightGrams
    return if (latest != null) "Gewicht ${formatDecimal(latest)} g" else "Kein Gewicht"
}

private fun latestLengthLabel(turtle: TurtleDetails): String {
    val latest = turtle.measurements.lastOrNull { it.carapaceLengthMm != null }?.carapaceLengthMm
    return if (latest != null) "Länge ${formatDecimal(latest)} mm" else "Keine Länge"
}

private fun eventMetaLabel(event: LifeEventRecord): String {
    val dateLabel = event.date.format(UI_DATE_FORMAT)
    return if (event.category.isBlank() || event.category == "Allgemein") {
        dateLabel
    } else {
        "$dateLabel | ${event.category}"
    }
}

private fun hatchDateLabel(hatchDate: HatchDateInfo?): String {
    return if (hatchDate == null) {
        "Schlupf offen"
    } else {
        "Schlupf ${formatHatchDateForDisplay(hatchDate)}"
    }
}

private fun ageLabel(hatchDate: HatchDateInfo?): String {
    if (hatchDate == null) return "Schlupfdatum noch nicht hinterlegt"

    val today = LocalDate.now()
    val days = ChronoUnit.DAYS.between(hatchDate.date, today)
    val period = Period.between(hatchDate.date, today)

    return when (hatchDate.precision) {
        HatchDatePrecision.DAY -> when {
            days < 120 -> "Alter: $days Tage"
            period.years > 0 -> "Alter: ${period.years} Jahre, ${period.months} Monate"
            else -> "Alter: ${period.months} Monate, ${period.days} Tage"
        }
        HatchDatePrecision.MONTH -> when {
            period.years > 0 -> "Alter: ca. ${period.years} Jahre, ${period.months} Monate"
            else -> "Alter: ca. ${period.months.coerceAtLeast(0)} Monate"
        }
    }
}

private fun formatHatchDateForInput(hatchDate: HatchDateInfo?): String {
    return when (hatchDate?.precision) {
        HatchDatePrecision.DAY -> hatchDate.date.format(INPUT_DATE_FORMAT)
        HatchDatePrecision.MONTH -> hatchDate.date.format(MONTH_YEAR_FORMAT)
        null -> ""
    }
}

private fun formatHatchDateForDisplay(hatchDate: HatchDateInfo): String {
    return when (hatchDate.precision) {
        HatchDatePrecision.DAY -> hatchDate.date.format(UI_DATE_FORMAT)
        HatchDatePrecision.MONTH -> hatchDate.date.format(MONTH_YEAR_FORMAT)
    }
}

private fun trashStatusLabel(trashedAt: Long?): String {
    if (trashedAt == null) return "Im Papierkorb"

    val deletedDate = Instant.ofEpochMilli(trashedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val autoDeleteDate = deletedDate.plusDays(TRASH_RETENTION_DAYS)
    val remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), autoDeleteDate).coerceAtLeast(0)
    val deletionHint = when (remainingDays) {
        0L -> "wird heute automatisch gelöscht"
        1L -> "wird in 1 Tag automatisch gelöscht"
        else -> "wird in $remainingDays Tagen automatisch gelöscht"
    }

    return "Gelöscht am ${deletedDate.format(UI_DATE_FORMAT)} • $deletionHint"
}

private fun formatDecimal(value: Float): String = DecimalFormat("#0.#").format(value)

private const val TRASH_RETENTION_DAYS = 30L
private val UI_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-uuuu")
private val SHORT_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/uu")
private val INPUT_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-uuuu")
private val MONTH_YEAR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-uuuu")
private val FULL_DATE_INPUT_FORMATTERS = listOf(
    DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ISO_LOCAL_DATE,
)
private val MONTH_YEAR_INPUT_FORMATTERS = listOf(
    DateTimeFormatter.ofPattern("MM-uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("MM.uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("MM/uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("M-uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("M.uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("M/uuuu").withResolverStyle(ResolverStyle.STRICT),
)
private val SHORT_MONTH_YEAR_REGEX = Regex("""^\s*(\d{1,2})\s*[/.-]\s*(\d{2})\s*$""")
