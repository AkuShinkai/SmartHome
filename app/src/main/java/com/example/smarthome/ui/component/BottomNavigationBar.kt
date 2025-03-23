package com.example.smarthome.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(
    selectedScreen: String,
    onItemSelected: (String) -> Unit
) {
    val PrimaryColor = Color(0xFF2AABD5)

    Box(
        modifier = Modifier.shadow(
            elevation = 8.dp, // Memberikan efek shadow
            shape = RectangleShape // Pastikan bentuk tetap persegi
        )
    ) {
        NavigationBar(
            containerColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = selectedScreen == "Home",
                onClick = { onItemSelected("Home") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = PrimaryColor,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Usage") },
                label = { Text("Usage") },
                selected = selectedScreen == "Usage",
                onClick = { onItemSelected("Usage") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = PrimaryColor,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Me") },
                label = { Text("Me") },
                selected = selectedScreen == "Me",
                onClick = { onItemSelected("Me") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = PrimaryColor,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(selectedScreen = "Home") {}
}