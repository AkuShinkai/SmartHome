package com.example.smarthome.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smarthome.ui.screens.AuthScreen
//import com.example.smarthome.ui.screens.HomeScreen
import com.example.smarthome.ui.screens.RegisterScreen

// Definisi route untuk setiap screen
sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object Home : Screen("home_screen")
    object Register : Screen("register_screen")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route // Halaman pertama saat aplikasi dijalankan
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(navController)
        }
//        composable(Screen.Home.route) {
//            HomeScreen(navController)
//        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
    }
}
