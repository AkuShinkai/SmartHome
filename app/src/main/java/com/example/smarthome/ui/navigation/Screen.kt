package com.example.smarthome.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object Home : Screen("home_screen")
    object Register : Screen("register_screen")

    // Tambahkan untuk start destination di MainActivity
    object Main : Screen("home_screen")  // Sesuai dengan login sukses
    object Login : Screen("auth_screen") // Sesuai dengan login gagal
}

