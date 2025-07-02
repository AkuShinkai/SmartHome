package com.example.smarthome.ui.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeviceViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference

    var switch1Status by mutableStateOf("OFF")
        private set
    var switch2Status by mutableStateOf("OFF")
        private set
    var powerValue by mutableStateOf(0.0)
        private set
    var switch_lamp by mutableStateOf("OFF")
        private set
    var powerLamp by mutableStateOf(0.0)
        private set
    var switch_door by mutableStateOf("OFF")
        private set
    var powerDoor by mutableStateOf(0.0)
    init {
        listenToSwitchStatus()
        listenToPower()
        listenToPowerDoor()
        listenToPowerLamp()
    }

    private fun listenToSwitchStatus() {
        database.child("socket_2_lubang/switch/status1").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                switch1Status = snapshot.getValue(String::class.java) ?: "OFF"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Gagal baca status1: ${error.message}")
            }
        })

        database.child("socket_2_lubang/switch/status2").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                switch2Status = snapshot.getValue(String::class.java) ?: "OFF"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Gagal baca status2: ${error.message}")
            }
        })

        database.child("smart_lamp/switch/status1").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                switch_lamp = snapshot.getValue(String::class.java) ?: "OFF"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Gagal baca status2: ${error.message}")
            }
        })

        database.child("smart_door/switch/status1").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                switch_door = snapshot.getValue(String::class.java) ?: "OFF"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Gagal baca status2: ${error.message}")
            }
        })
    }

    private fun listenToPower() {
        database.child("socket_2_lubang/sensors/power")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val power = snapshot.getValue(Double::class.java)
                    powerValue = power ?: 0.0
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Gagal baca power: ${error.message}")
                }
            })
    }

    private fun listenToPowerDoor() {
        database.child("smart_door/sensors/power")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val power = snapshot.getValue(Double::class.java)
                    powerDoor = power ?: 0.0
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Gagal baca power: ${error.message}")
                }
            })
    }

    private fun listenToPowerLamp() {
        database.child("smart_lamp/sensors/power")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val power = snapshot.getValue(Double::class.java)
                    powerLamp = power ?: 0.0
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Gagal baca power: ${error.message}")
                }
            })
    }
}

fun observeStateChange(
    devicePath: String,
    expectedState: String,
    timeoutMillis: Long = 4000,
    onResult: (Boolean) -> Unit
) {
    val database = FirebaseDatabase.getInstance().getReference(devicePath)
    var resultHandled = false

    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val currentState = snapshot.getValue(String::class.java)
            if (currentState == expectedState && !resultHandled) {
                resultHandled = true
                onResult(true)
                database.removeEventListener(this)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            if (!resultHandled) {
                resultHandled = true
                onResult(false)
            }
        }
    }

    database.addValueEventListener(listener)

    // Timeout handler
    Handler(Looper.getMainLooper()).postDelayed({
        if (!resultHandled) {
            resultHandled = true
            database.removeEventListener(listener)
            onResult(false)
        }
    }, timeoutMillis)
}

data class Device(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val room: String
)

data class DeviceSchedule(
    val time: String = "",
    val action: String = ""
)

data class DeviceSensorData(
    val power: Double = 0.0,
    val current: Double = 0.0,
    val voltage: Double = 0.0,
    val energy: Double = 0.0
)
