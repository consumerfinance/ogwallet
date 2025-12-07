package dev.consumerfinance.ogwallet

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.datetime.*

actual class ReminderScheduler(private val context: Context) {

    actual fun schedule(id: String, title: String, body: String, dateTime: LocalDateTime) {

        val millis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setExact(AlarmManager.RTC_WAKEUP, millis, pending)
    }
}
