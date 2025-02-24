package com.example.smarthome.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.smarthome.ui.component.BottomNavigationBar

@Composable
fun MainScreen(navController: NavController) {
    var selectedScreen by remember { mutableStateOf("Home") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onItemSelected = { screen ->
                    selectedScreen = screen
                }
            )
        }
    ) { paddingValues -> // ✅ Pastikan parameter ini digunakan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues) // ✅ Tambahkan paddingValues agar tidak error
        ) {
            when (selectedScreen) {
                "Home" -> HomeScreen(navController)
                "Usage" -> UsageScreen(navController)
                "Me" -> MeScreen(navController)
            }
        }
    }
}

