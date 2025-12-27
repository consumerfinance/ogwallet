package dev.consumerfinance.ogwallet.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.consumerfinance.ogwallet.MainActivity
import dev.consumerfinance.ogwallet.R
import dev.consumerfinance.ogwallet.utils.formatCurrency

class BillReminderNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "bill_reminders"
        private const val CHANNEL_NAME = "Bill Payment Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming credit card bill payments"
        private const val NOTIFICATION_ID_BASE = 1000
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun sendBillReminderNotification(
        billId: Int,
        cardName: String,
        dueAmount: Double,
        dueDate: String,
        daysRemaining: Int,
        currencyCode: String = "INR"
    ) {
        // Check for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                android.util.Log.w("BillReminder", "Notification permission not granted")
                return
            }
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_bills", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            billId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = when (daysRemaining) {
            0 -> "⚠️ Bill Due Today!"
            1 -> "⏰ Bill Due Tomorrow"
            else -> "📅 Bill Due in $daysRemaining Days"
        }
        
        val message = "$cardName payment of ${formatCurrency(dueAmount, currencyCode)} is due on $dueDate"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with app icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + billId,
                notification
            )
            android.util.Log.d("BillReminder", "Notification sent for bill $billId")
        } catch (e: SecurityException) {
            android.util.Log.e("BillReminder", "Failed to send notification: ${e.message}")
        }
    }
    
    fun cancelNotification(billId: Int) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_BASE + billId)
    }
    
    fun sendTestNotification() {
        sendBillReminderNotification(
            billId = 999,
            cardName = "HDFC Credit Card XX1234",
            dueAmount = 15000.0,
            dueDate = "28 Dec 2024",
            daysRemaining = 3
        )
    }
}

