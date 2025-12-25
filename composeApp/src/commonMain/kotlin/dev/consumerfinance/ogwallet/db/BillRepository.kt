package dev.consumerfinance.ogwallet.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

class BillRepository(private val dbManager: DatabaseManager) {
    
    /**
     * Creates a new credit card bill
     */
    suspend fun createBill(
        cardHandle: String,
        cardName: String,
        billingCycleStart: Instant,
        billingCycleEnd: Instant,
        dueDate: Instant,
        totalAmount: Double,
        minimumDue: Double
    ) = withContext(Dispatchers.Default) {
        dbManager.queries?.insertBill(
            card_handle = cardHandle,
            card_name = cardName,
            billing_cycle_start = billingCycleStart.toEpochMilliseconds(),
            billing_cycle_end = billingCycleEnd.toEpochMilliseconds(),
            due_date = dueDate.toEpochMilliseconds(),
            total_amount = totalAmount,
            minimum_due = minimumDue
        )
    }
    
    /**
     * Gets all bills
     */
    fun getAllBills(): Flow<List<dev.ogwallet.db.Credit_card_bill>> = flow {
        val bills = withContext(Dispatchers.Default) {
            dbManager.queries?.getAllBills()?.executeAsList() ?: emptyList()
        }
        emit(bills)
    }
    
    /**
     * Gets upcoming unpaid bills within a date range
     */
    suspend fun getUpcomingBills(
        startDate: Instant,
        endDate: Instant
    ): List<dev.ogwallet.db.Credit_card_bill> = withContext(Dispatchers.Default) {
        dbManager.queries?.getUpcomingBills(
            startDate.toEpochMilliseconds(),
            endDate.toEpochMilliseconds()
        )?.executeAsList() ?: emptyList()
    }
    
    /**
     * Gets bills due within the specified number of days
     */
    suspend fun getBillsDueInDays(days: Int): List<dev.ogwallet.db.Credit_card_bill> = 
        withContext(Dispatchers.Default) {
            val thresholdDate = Clock.System.now() + days.days
            dbManager.queries?.getBillsDueInDays(
                thresholdDate.toEpochMilliseconds()
            )?.executeAsList() ?: emptyList()
        }
    
    /**
     * Marks a bill as paid
     */
    suspend fun markBillPaid(
        billId: Long,
        paidAmount: Double,
        paidDate: Instant = Clock.System.now()
    ) = withContext(Dispatchers.Default) {
        dbManager.queries?.markBillPaid(
            paid_amount = paidAmount,
            paid_date = paidDate.toEpochMilliseconds(),
            id = billId
        )
    }
    
    /**
     * Marks that a reminder has been sent for a bill
     */
    suspend fun markReminderSent(billId: Long) = withContext(Dispatchers.Default) {
        dbManager.queries?.markReminderSent(billId)
    }
    
    /**
     * Creates a sample bill for testing (due in 3 days)
     */
    suspend fun createSampleBill() {
        val now = Clock.System.now()
        val dueDate = now + 3.days
        val cycleStart = now - 27.days
        val cycleEnd = now + 3.days
        
        createBill(
            cardHandle = "1234",
            cardName = "HDFC Credit Card XX1234",
            billingCycleStart = cycleStart,
            billingCycleEnd = cycleEnd,
            dueDate = dueDate,
            totalAmount = 15000.0,
            minimumDue = 750.0
        )
    }
}

