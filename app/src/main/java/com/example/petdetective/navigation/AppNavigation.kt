package com.example.petdetective.navigation

import androidx.compose.runtime.Composable
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

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onReportLost = { navController.navigate("report/LOST") },
                onReportFound = { navController.navigate("report/FOUND") },
                onViewList = { navController.navigate("list") }
            )
        }

        composable("report/{type}") { backStack ->
            val type = backStack.arguments?.getString("type") ?: "LOST"
            ReportScreen(
                type = type,
                onBack = { navController.popBackStack() },
                onSave = { report ->
                    petViewModel.addReport(report)
                    NotificationHelper.showNotification(context, report.name)
                    navController.popBackStack()
                }
            )
        }

        composable("list") {
            PetListScreen(
                viewModel = petViewModel,
                onPetClick = { id -> navController.navigate("detail/$id") }
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