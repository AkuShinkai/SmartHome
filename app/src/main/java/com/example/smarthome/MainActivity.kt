package com.example.smarthome

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.navigation.NavGraph
import com.example.smarthome.session.SessionManager
import com.example.smarthome.ui.navigation.Screen
import com.example.smarthome.ui.theme.SmartHomeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity(), DefaultLifecycleObserver {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ComponentActivity>.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        // Mendaftarkan observer lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        lifecycleScope.launch {
            val isLoggedIn = sessionManager.isLoggedIn.first()

            setContent {
                SmartHomeTheme {
                    Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            startDestination = if (isLoggedIn) Screen.Main.route else Screen.Login.route
                        )
                    }
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        // Simpan waktu terakhir aplikasi digunakan
        lifecycleScope.launch {
            sessionManager.updateLastActiveTime()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
        lifecycleScope.launch {
            val isLoggedIn = sessionManager.isLoggedIn.first()
            if (!isLoggedIn) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Session habis!", Toast.LENGTH_LONG).show()
                    setContent {
                        val navController = rememberNavController()
                        NavGraph(navController = navController, startDestination = Screen.Login.route)
                    }
                }
            }
        }
    }
}