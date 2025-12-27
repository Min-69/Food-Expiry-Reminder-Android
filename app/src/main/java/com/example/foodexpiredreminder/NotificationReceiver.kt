package com.example.foodexpiredreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Ambil data produk dari intent
        val productName = intent.getStringExtra("EXTRA_PRODUCT_NAME") ?: "Produk"
        val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", 0)

        // 2. Buat Notification Manager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 3. Buat Channel Notifikasi (Wajib untuk Android Oreo ke atas)
        val channelId = "food_reminder_channel"
        val channelName = "Pengingat Makanan"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        // 4. Buat notifikasi itu sendiri
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan ikon notifikasi Anda
            .setContentTitle("Produk Hampir Kedaluwarsa!")
            .setContentText("$productName akan segera kedaluwarsa. Segera periksa!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Notifikasi hilang saat disentuh
            .build()

        // 5. Tampilkan notifikasi
        notificationManager.notify(notificationId, notification)
    }
}