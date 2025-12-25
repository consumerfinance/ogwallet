package dev.consumerfinance.ogwallet.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.consumerfinance.ogwallet.db.BillRepository
import dev.ogwallet.db.Credit_card_bill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.days

class BillReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val billRepository: BillRepository by inject()

    companion object {
        const val WORK_NAME = "bill_reminder_check"
        private const val TAG = "BillReminderWorker"
        private const val REMINDER_DAYS_BEFORE = 3
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting bill reminder check...")

            val notificationManager = BillReminderNotificationManager(applicationContext)

            // Get bills due within the next 3 days that haven't been reminded yet
            val upcomingBills = billRepository.getBillsDueInDays(REMINDER_DAYS_BEFORE)

            if (upcomingBills.isEmpty()) {
                Log.d(TAG, "No upcoming bills found")
                return@withContext Result.success()
            }

            Log.d(TAG, "Found ${upcomingBills.size} bills requiring reminders")

            val now = Clock.System.now()

            upcomingBills.forEach { bill ->
                val dueDate = Instant.fromEpochMilliseconds(bill.due_date)
                val daysRemaining = calculateDaysRemaining(now, dueDate)

                // Only send reminder if due within 3 days
                if (daysRemaining <= REMINDER_DAYS_BEFORE) {
                    Log.d(TAG, "Sending reminder for bill ${bill.id}: ${bill.card_name}")

                    notificationManager.sendBillReminderNotification(
                        billId = bill.id.toInt(),
                        cardName = bill.card_name,
                        dueAmount = bill.total_amount,
                        dueDate = formatDate(dueDate),
                        daysRemaining = daysRemaining
                    )

                    // Mark reminder as sent
                    billRepository.markReminderSent(bill.id)
                }
            }

            Log.d(TAG, "Bill reminder check completed successfully")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Error checking bill reminders: ${e.message}", e)
            Result.retry()
        }
    }
    
    private fun calculateDaysRemaining(now: Instant, dueDate: Instant): Int {
        val diff = dueDate.toEpochMilliseconds() - now.toEpochMilliseconds()
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
    
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun formatDate(instant: Instant): String {
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val months = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${localDate.dayOfMonth} ${months[localDate.monthNumber - 1]} ${localDate.year}"
    }
}

