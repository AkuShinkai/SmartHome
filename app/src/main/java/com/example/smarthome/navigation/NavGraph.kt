package com.example.smarthome.navigation

//import com.example.smarthome.ui.screens.HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smarthome.ui.screens.AuthScreen
import com.example.smarthome.ui.screens.FaceRegisterScreen
import com.example.smarthome.ui.screens.MainScreen
import com.example.smarthome.ui.screens.RegisterScreen

// Definisi route untuk setiap screen
sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object Home : Screen("home_screen")
    object Register : Screen("register_screen")
    object FaceRegister : Screen("face_register_screen")
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
        composable(Screen.Home.route) {
            MainScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable("face_register_screen/{name}/{email}/{password}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            FaceRegisterScreen(navController, name, email, password)
        }
    }
}
