package com.example.petdetective.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petdetective.ui.screens.*
import com.example.petdetective.ui.utils.NotificationHelper
import com.example.petdetective.viewmodel.PetViewModel

@Composable
fun AppNavigation(petViewModel: PetViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isSaving by petViewModel.isSaving.collectAsState()
    val errorMessage by petViewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            petViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {

            composable("home") {
                HomeScreen(
                    viewModel = petViewModel,
                    onReportLost = { navController.navigate("report/LOST") },
                    onReportFound = { navController.navigate("report/FOUND") },
                    onViewList = { navController.navigate("list") },
                    onViewMyReports = { navController.navigate("my_reports") },
                    onPetClick = { id -> navController.navigate("detail/$id") }
                )
            }

            composable("report/{type}") { backStack ->
                val type = backStack.arguments?.getString("type") ?: "LOST"
                ReportScreen(
                    type = type,
                    isSaving = isSaving,
                    onBack = { navController.popBackStack() },
                    onSave = { report ->
                        petViewModel.addReport(report) {
                            NotificationHelper.showNotification(context, report.name)
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable("list") {
                PetListScreen(
                    viewModel = petViewModel,
                    onPetClick = { id -> navController.navigate("detail/$id") }
                )
            }

            composable("my_reports") {
                MyReportsScreen(
                    viewModel = petViewModel,
                    onPetClick = { id -> navController.navigate("detail/$id") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("detail/{id}") { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                PetDetailScreen(
                    petId = id,
                    viewModel = petViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}