package com.example.smarthome.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

data class UsageData(
    val device: String = "", // NEW: Tambahkan nama device
    val power: Float = 0f,
    val energy: Float = 0f,
    val current: Float = 0f,
    val timestamp: Timestamp = Timestamp.now()
)

suspend fun fetchAllUsageData(): List<UsageData> {
    val firestore = Firebase.firestore
    val deviceList = listOf("smart_lamp", "socket_2_lubang", "smart_door")

    val allData = mutableListOf<UsageData>()

    for (device in deviceList) {
        try {
            val snapshot = firestore
                .collection("history")
                .document(device)
                .collection("sensors")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d("UsageScreen", "No data for $device")
                continue
            }

            val deviceData = snapshot.documents.mapNotNull { doc ->
                val after = doc.get("after") as? Map<*, *> ?: return@mapNotNull null
                val before = doc.get("before") as? Map<*, *> ?: return@mapNotNull null

                val afterEnergy = (after["energy"] as? Number)?.toFloat() ?: return@mapNotNull null
                val beforeEnergy = (before["energy"] as? Number)?.toFloat() ?: return@mapNotNull null
                val deltaEnergy = (afterEnergy - beforeEnergy).coerceAtLeast(0f)

                val power = (after["power"] as? Number)?.toFloat() ?: 0f
                val current = (after["current"] as? Number)?.toFloat() ?: 0f
                val timestamp = doc.getTimestamp("timestamp") ?: return@mapNotNull null

                UsageData(device, power, deltaEnergy, current, timestamp)
            }

            Log.d("UsageScreen", "Fetched ${deviceData.size} entries for $device")
            allData.addAll(deviceData)

        } catch (e: Exception) {
            Log.e("UsageScreen", "Gagal fetch data untuk $device", e)
        }
    }

    return allData
}

fun calculateTotalEnergy(data: List<UsageData>): Double {
    val total = data.sumOf { it.energy.toDouble() }
    Log.d("UsageScreen", "Total energy (Wh): $total")
    return total
}