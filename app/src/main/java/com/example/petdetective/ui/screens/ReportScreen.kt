package com.example.petdetective.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petdetective.data.model.PetReport
import com.example.petdetective.data.model.PetSpecies
import com.example.petdetective.data.model.ReportType
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    type: String,
    onBack: () -> Unit,
    onSave: (PetReport) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var species by remember { mutableStateOf(PetSpecies.DOG) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            isLocating = true
            scope.launch {
                try {
                    @SuppressLint("MissingPermission")
                    val locationResult = fusedLocationClient.lastLocation.await()
                    if (locationResult != null) {
                        latitude = locationResult.latitude
                        longitude = locationResult.longitude
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLocating = false
                }
            }
        }
    }

    val isFormValid = name.isNotBlank() && location.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (type == "LOST") "Perdí a mi mascota" else "Encontré una mascota") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Especie", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PetSpecies.entries.forEach { sp ->
                    FilterChip(
                        selected = species == sp,
                        onClick = { species = sp },
                        label = { Text(sp.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la mascota *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción / Detalles") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación (barrio, calle, referencia) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Teléfono de contacto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (photoUri == null) "Seleccionar Foto" else "Cambiar Foto")
            }

            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto de la mascota",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLocating
            ) {
                Text(if (isLocating) "Obteniendo ubicación..." else "Capturar Ubicación GPS")
            }

            if (latitude != null && longitude != null) {
                Text("GPS capturado: $latitude, $longitude", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val report = PetReport(
                        type = if (type == "LOST") ReportType.LOST else ReportType.FOUND,
                        species = species,
                        name = name,
                        description = description,
                        location = location,
                        contactPhone = contactPhone,
                        photoUri = photoUri,
                        latitude = latitude,
                        longitude = longitude
                    )
                    onSave(report)
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Reporte")
            }
        }
    }
}