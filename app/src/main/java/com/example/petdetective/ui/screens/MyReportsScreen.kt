package com.example.petdetective.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petdetective.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    viewModel: PetViewModel,
    onPetClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val allReports by viewModel.reports.collectAsState()
    val myReports = allReports.filter { it.userId == viewModel.currentUserId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reportes") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Volver") } }
            )
        }
    ) { padding ->
        if (myReports.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No has creado ningún reporte todavía.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myReports) { report ->
                    PetCard(report = report, onClick = { onPetClick(report.id) })
                }
            }
        }
    }
}