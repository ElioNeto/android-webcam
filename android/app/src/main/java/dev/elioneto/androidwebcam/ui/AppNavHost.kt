package dev.elioneto.androidwebcam.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.elioneto.androidwebcam.ui.home.HomeScreen
import dev.elioneto.androidwebcam.ui.settings.SettingsScreen
import dev.elioneto.androidwebcam.ui.streaming.StreamingScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Streaming : Screen("streaming")
    object Settings : Screen("settings")
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartStreaming = { navController.navigate(Screen.Streaming.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Streaming.route) {
            StreamingScreen(onStop = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
