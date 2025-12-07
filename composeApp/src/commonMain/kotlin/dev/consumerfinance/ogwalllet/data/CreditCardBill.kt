// Shared data model (commonMain)
data class CreditCardBill(
    val id: Int,
    val name: String,
    val dueDate: Long,  // In milliseconds
    val amount: Double,
    val isPaid: Boolean = false
)

class BillReminderService {

    private val bills = mutableListOf<CreditCardBill>()

    // Add a new bill to the list
    fun addBill(bill: CreditCardBill) {
        bills.add(bill)
    }

    // Get upcoming bills
    fun getUpcomingBills(): List<CreditCardBill> {
        val currentTime = System.currentTimeMillis()
        return bills.filter { it.dueDate > currentTime && !it.isPaid }
    }

    // Mark bill as paid
    fun markAsPaid(billId: Int) {
        val bill = bills.find { it.id == billId }
        bill?.let {
            it.copy(isPaid = true)
        }
    }

    // Get past bills (already paid)
    fun getPaidBills(): List<CreditCardBill> {
        return bills.filter { it.isPaid }
    }
}
