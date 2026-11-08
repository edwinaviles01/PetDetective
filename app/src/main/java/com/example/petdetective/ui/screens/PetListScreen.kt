package com.example.petdetective.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petdetective.data.model.PetReport
import com.example.petdetective.data.model.PetSpecies
import com.example.petdetective.data.model.ReportType
import com.example.petdetective.viewmodel.PetViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(
    viewModel: PetViewModel,
    onPetClick: (String) -> Unit
) {
    val reports by viewModel.reports.collectAsState()
    var selectedType by remember { mutableStateOf<ReportType?>(null) }
    var selectedSpecies by remember { mutableStateOf<PetSpecies?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var isNearMeEnabled by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            isLocating = true
            scope.launch {
                try {
                    @SuppressLint("MissingPermission")
                    val loc = fusedLocationClient.lastLocation.await()
                    userLocation = loc
                    if (loc != null) isNearMeEnabled = true
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLocating = false
                }
            }
        }
    }

    val filteredReports = reports.filter { report ->
        val matchesType = selectedType == null || report.type == selectedType
        val matchesSpecies = selectedSpecies == null || report.species == selectedSpecies
        val matchesSearch = searchQuery.isBlank() ||
                report.name.contains(searchQuery, ignoreCase = true) ||
                report.location.contains(searchQuery, ignoreCase = true)

        var matchesDistance = true
        if (isNearMeEnabled) {
            if (userLocation != null && report.latitude != null && report.longitude != null) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLocation!!.latitude, userLocation!!.longitude,
                    report.latitude, report.longitude,
                    results
                )
                matchesDistance = results[0] <= 10000f
            } else {
                matchesDistance = false
            }
        }
        matchesType && matchesSpecies && matchesSearch && matchesDistance
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reportes") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre o ubicación") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = selectedType == null, onClick = { selectedType = null }, label = { Text("Todos") })
                    FilterChip(selected = selectedType == ReportType.LOST, onClick = { selectedType = ReportType.LOST }, label = { Text("Perdidos") })
                    FilterChip(selected = selectedType == ReportType.FOUND, onClick = { selectedType = ReportType.FOUND }, label = { Text("Encontrados") })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(selected = selectedSpecies == null, onClick = { selectedSpecies = null }, label = { Text("Todas") })
                        }
                        items(PetSpecies.entries) { sp ->
                            FilterChip(selected = selectedSpecies == sp, onClick = { selectedSpecies = sp }, label = { Text(sp.label) })
                        }
                    }
                }

                FilterChip(
                    selected = isNearMeEnabled,
                    onClick = {
                        if (isNearMeEnabled) {
                            isNearMeEnabled = false
                        } else {
                            if (userLocation != null) {
                                isNearMeEnabled = true
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        }
                    },
                    label = { Text(if (isLocating) "Buscando GPS..." else "Cerca de mí (10km)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    enabled = !isLocating
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            if (filteredReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay reportes que coincidan.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReports) { report ->
                        PetCard(report = report, onClick = { onPetClick(report.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun PetCard(report: PetReport, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .alpha(if (report.isResolved) 0.6f else 1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            if (report.photoUri != null) {
                AsyncImage(
                    model = report.photoUri,
                    contentDescription = "Foto",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val statusText = if (report.type == ReportType.LOST) "PERDIDO" else "ENCONTRADO"
                    val statusColor = if (report.type == ReportType.LOST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

                    if (report.isResolved) {
                        Text("RESUELTO", color = androidx.compose.ui.graphics.Color(0xFF4CAF50), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    } else {
                        Text(report.species.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(report.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(report.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)

                if (report.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(report.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
        }
    }
}