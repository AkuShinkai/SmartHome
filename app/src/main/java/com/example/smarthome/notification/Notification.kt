package com.example.smarthome.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smarthome.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notification = remoteMessage.notification
        if (notification != null) {
            sendNotification(notification.title ?: "Pemberitahuan", notification.body ?: "")
        }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "fire_alert_channel"

        // Buat channel jika belum ada (penting untuk Android 8+)
        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Peringatan Kebakaran")
                .setDescription("Notifikasi untuk peringatan kebakaran dari sistem smart home")
                .build()
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_no_text) // Ganti dengan icon kecil yang valid
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Cek izin sebelum memanggil notify() - WAJIB Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(0, builder.build())
        } else {
            Log.w("FCM", "Notifikasi tidak ditampilkan karena izin POST_NOTIFICATIONS belum diberikan.")
        }
    }
}
