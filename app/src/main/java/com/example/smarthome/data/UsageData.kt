package com.example.smarthome.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UsageData(
    val timestamp: Timestamp,
    val power: Double,
    val energy: Double,
    val current: Double,
    val voltage: Double
)

suspend fun fetchUsageData(switchId: String): List<UsageData> {
    val db = FirebaseFirestore.getInstance()
    val usageDataList = mutableListOf<UsageData>()

    try {
        val querySnapshot = db.collection("usage")
            .document("saklar")
            .collection(switchId)
            .orderBy("timestamp") // Urutkan berdasarkan waktu
            .get()
            .await()

        Log.d("Firestore", "Data ditemukan: ${querySnapshot.documents.size}")

        for (document in querySnapshot.documents) {
            val timestamp = document.getTimestamp("timestamp") ?: Timestamp.now()
            val power = document.getDouble("power") ?: 0.0
            val energy = document.getDouble("energy") ?: 0.0
            val current = document.getDouble("current") ?: 0.0
            val voltage = document.getDouble("voltage") ?: 0.0

            usageDataList.add(UsageData(timestamp, power, energy, current, voltage))
        }

        if (usageDataList.isEmpty()) {
            Log.w("Firestore", "Tidak ada data yang ditemukan")
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error fetching data", e)
    }

    return usageDataList
}
