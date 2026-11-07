package com.example.petdetective.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onViewList: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PetDetective") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Bienvenido a PetDetective", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onReportLost, modifier = Modifier.fillMaxWidth()) {
                Text("Reportar mascota perdida")
            }
            Button(onClick = onReportFound, modifier = Modifier.fillMaxWidth()) {
                Text("Reportar mascota encontrada")
            }
            OutlinedButton(onClick = onViewList, modifier = Modifier.fillMaxWidth()) {
                Text("Ver reportes")
            }
        }
    }
}