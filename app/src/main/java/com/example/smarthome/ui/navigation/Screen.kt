package com.example.smarthome.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object Login : Screen("login_screen")
    object FaceLogin : Screen("face_login_screen")
    object Register : Screen("register_screen")
    object FaceRegister : Screen("face_register_screen")
    object Home : Screen("home_screen")
}
