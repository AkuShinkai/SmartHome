package com.example.smarthome.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BottomNavigationBar(
    selectedScreen: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedScreen == "Home",
            onClick = { onItemSelected("Home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Bolt, contentDescription = "Usage") },
            label = { Text("Usage") },
            selected = selectedScreen == "Usage",
            onClick = { onItemSelected("Usage") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Me") },
            label = { Text("Me") },
            selected = selectedScreen == "Me",
            onClick = { onItemSelected("Me") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(selectedScreen = "Home") {}
}
