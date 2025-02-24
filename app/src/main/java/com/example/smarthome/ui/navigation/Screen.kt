package com.example.smarthome.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object Register : Screen("register_screen")
    object Home : Screen("home_screen")
}
