package com.example.smarthome

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.data.MqttManager
import com.example.smarthome.navigation.NavGraph
import com.example.smarthome.session.SessionManager
import com.example.smarthome.ui.navigation.Screen
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.google.firebase.messaging.FirebaseMessaging
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
        MqttManager.connect()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        FirebaseMessaging.getInstance().subscribeToTopic("gas_alert")
        FirebaseMessaging.getInstance().subscribeToTopic("fire_alert")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        lifecycleScope.launch {
            val isLoggedIn = sessionManager.isLoggedIn.first()
            val isExpired = sessionManager.isSessionExpired()

            val startDestination = if (isLoggedIn && !isExpired) {
                Screen.Main.route
            } else {
                if (isLoggedIn && isExpired) {
                    sessionManager.clearSessionData()
                }
                Screen.Login.route
            }

            // Setelah tahu hasilnya, baru set content
            setContent {
                SmartHomeTheme {
                    Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination
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
            val isExpired = sessionManager.isSessionExpired()

            if (isLoggedIn && isExpired) {
                Toast.makeText(applicationContext, "Sesi habis", Toast.LENGTH_LONG).show()
            }

            // Selalu update waktu aktif, agar sesi diperpanjang jika masih aktif
            sessionManager.updateLastActiveTime()
        }
    }

    override fun onDestroy() {
        super<ComponentActivity>.onDestroy()
        // ❌ Putuskan koneksi saat aplikasi ditutup
        MqttManager.disconnect()
    }
}