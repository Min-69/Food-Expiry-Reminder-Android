package com.example.foodexpiredreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.example.foodexpiredreminder.SettingsActivity.Companion.NOTIFICATION_DAYS
import com.example.foodexpiredreminder.SettingsActivity.Companion.NOTIFICATION_ENABLED
import com.example.foodexpiredreminder.SettingsActivity.Companion.NOTIFICATION_TIME
import com.example.foodexpiredreminder.SettingsActivity.Companion.PREFS_NAME
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun schedule(product: Product) {
        val baseRequestCode = product.id.hashCode()

        if (prefs.getBoolean(NOTIFICATION_ENABLED, true)) {
            val days = prefs.getString(NOTIFICATION_DAYS, "3")?.toIntOrNull() ?: 3
            val time = prefs.getString(NOTIFICATION_TIME, "0900") ?: "0900"
            val (hour, minute) = parseTime(time, 9, 0)
            scheduleReminder(product, days, hour, minute, "NOTIFICATION", baseRequestCode)
        }
    }

    private fun parseTime(time: String, defaultHour: Int, defaultMinute: Int): Pair<Int, Int> {
        return try {
            if (time.length == 4) {
                val hour = time.substring(0, 2).toInt()
                val minute = time.substring(2, 4).toInt()
                Pair(hour, minute)
            } else {
                Pair(defaultHour, defaultMinute)
            }
        } catch (e: Exception) {
            Pair(defaultHour, defaultMinute) // Fallback to default
        }
    }

    private fun scheduleReminder(product: Product, days: Int, hour: Int, minute: Int, type: String, requestCode: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = product.expiryDate
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis > System.currentTimeMillis()) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("EXTRA_PRODUCT_NAME", product.name)
                putExtra("REMINDER_TYPE", type)
                putExtra("EXTRA_NOTIFICATION_ID", requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }
    }

    fun cancel(product: Product) {
        val baseRequestCode = product.id.hashCode()
        cancelReminder(baseRequestCode)
    }

    private fun cancelReminder(requestCode: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}