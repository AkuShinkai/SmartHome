package com.example.smarthome.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.smarthome.navigation.Screen
import com.example.smarthome.session.SessionManager
import com.example.smarthome.ui.component.BottomNavigationBar
import kotlinx.coroutines.flow.first

@Composable
fun MainScreen(navController: NavController) {
    var selectedScreen by remember { mutableStateOf("Home") }
    var backPressedTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // ✅ Cek apakah sesi habis
    LaunchedEffect(Unit) {
        val isLoggedIn = sessionManager.isLoggedIn.first()
        val isExpired = sessionManager.isSessionExpired()

        if (isLoggedIn && isExpired) {
            sessionManager.clearSessionData()

            // Navigate ke AuthScreen dan hapus stack sebelumnya
            navController.navigate(Screen.Auth.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // ⏮️ Tombol Back ganda untuk keluar
    BackHandler {
        if (selectedScreen != "Home") {
            selectedScreen = "Home"
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                (context as? Activity)?.finish()
            } else {
                backPressedTime = currentTime
                Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🧭 Tampilan dan navigasi bawah
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onItemSelected = { screen ->
                    selectedScreen = screen
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (selectedScreen) {
                "Home" -> HomeScreen(navController)
                "Usage" -> UsageScreen(navController)
                "Me" -> MeScreen(navController)
            }
        }
    }
}
