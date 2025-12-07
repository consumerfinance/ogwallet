package dev.consumerfinance.ogwallet.data

class BillReminderService {
    private val bills = mutableListOf<CreditCardBill>()

    fun addBill(bill: CreditCardBill) {
        bills.add(bill)
    }

    fun getUpcomingBills(): List<CreditCardBill> {
        val currentTime = System.currentTimeMillis()
        return bills.filter { it.dueDate > currentTime && !it.isPaid }
    }

    fun markAsPaid(billId: Int) {
        val bill = bills.find { it.id == billId }
        bill?.let {
            it.copy(isPaid = true)
        }
    }

    fun getPaidBills(): List<CreditCardBill> {
        return bills.filter { it.isPaid }
    }
}