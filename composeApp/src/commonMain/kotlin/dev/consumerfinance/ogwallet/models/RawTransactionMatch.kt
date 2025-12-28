package dev.consumerfinance.ogwallet.models

data class RawTransactionMatch(
    val amount: Double,
    val currency: String,
    val accountHandle: String,
    val merchantRaw: String,
    val category: String,
    val transactionType: TransactionType,
    val rawBody: String
)

enum class TransactionType {
    DEBIT,
    CREDIT
}

