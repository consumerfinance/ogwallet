package dev.consumerfinance.ogwallet.notifications

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object BillReminderScheduler {
    
    private const val TAG = "BillReminderScheduler"
    
    /**
     * Schedules a daily check for upcoming bill payments.
     * The worker will run once per day and check for bills due within 3 days.
     */
    fun scheduleDailyBillCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
            flexTimeInterval = 2,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // First check after 1 hour
            .addTag("bill_reminders")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BillReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
        
        Log.d(TAG, "Daily bill reminder check scheduled")
    }
    
    /**
     * Triggers an immediate one-time check for bill reminders.
     * Useful for testing or manual triggers.
     */
    fun triggerImmediateCheck(context: Context) {
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<BillReminderWorker>()
            .addTag("bill_reminders_immediate")
            .build()
        
        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
        
        Log.d(TAG, "Immediate bill reminder check triggered")
    }
    
    /**
     * Cancels all scheduled bill reminder checks.
     */
    fun cancelAllReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(BillReminderWorker.WORK_NAME)
        Log.d(TAG, "All bill reminders cancelled")
    }
    
    /**
     * Checks if bill reminders are currently scheduled.
     */
    fun isScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(BillReminderWorker.WORK_NAME)
            .get()
        
        return workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }
}

