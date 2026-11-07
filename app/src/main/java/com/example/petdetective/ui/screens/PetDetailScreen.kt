package com.example.petdetective.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petdetective.data.model.ReportType
import com.example.petdetective.viewmodel.PetViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: String,
    viewModel: PetViewModel,
    onBack: () -> Unit
) {
    val report = viewModel.getReportById(petId)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar reporte") },
            text = { Text("¿Estás seguro de que deseas eliminar este reporte permanentemente?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReport(petId)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Volver") }
                },
                actions = {
                    if (report != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar reporte"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (report == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Reporte no encontrado")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (report.photoUri != null) {
                    AsyncImage(
                        model = report.photoUri,
                        contentDescription = "Foto de ${report.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                val statusText = if (report.type == ReportType.LOST) "MASCOTA PERDIDA" else "MASCOTA ENCONTRADA"
                val statusColor = if (report.type == ReportType.LOST) Color(0xFFC62828) else Color(0xFF2E7D32)

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = report.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Especie: ${report.species.label}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = report.location, style = MaterialTheme.typography.bodyLarge)

                if (report.latitude != null && report.longitude != null) {
                    val petLocation = LatLng(report.latitude, report.longitude)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(petLocation, 15f)
                    }

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = rememberMarkerState(position = petLocation),
                            title = report.name,
                            snippet = report.location
                        )
                    }
                }

                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (report.description.isBlank()) "Sin descripción adicional." else report.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Contacto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (report.contactPhone.isBlank()) "No se proporcionó teléfono." else report.contactPhone,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = report.contactPhone.isNotBlank()
                ) {
                    Text("Llamar a este contacto")
                }
            }
        }
    }
}