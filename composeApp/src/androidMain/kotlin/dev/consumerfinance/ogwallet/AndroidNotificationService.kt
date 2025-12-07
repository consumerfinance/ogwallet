import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AndroidNotificationService(private val context: Context) {

    fun showReminderNotification(bill: CreditCardBill) {
        val notificationId = bill.id

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Credit Card Bill Reminder")
            .setContentText("Your ${bill.name} bill of \$${bill.amount} is due soon!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification)
        }
    }

    // Create a Notification Channel (for Android 8.0 and above)
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Bill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for bill reminders"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
