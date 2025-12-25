package dev.consumerfinance.ogwallet.models.transactions

import kotlin.time.ExperimentalTime

data class TransactionEntry @OptIn(ExperimentalTime::class) constructor(
    val id: Int = 0,
    val amount: Double,
    val currency: String = "INR",
    val merchant: String,
    val timestamp: kotlinx.datetime.Instant,
    val category: TransactionCategory,
    val rawMessageId: String? = null, // Reference to the original SMS/Email ID
    val emoji: String,
    val rawBody: String,
    val type: TransactionType
)

// The different spending buckets
enum class TransactionCategory {
    FOOD, SHOPPING, BILLS, TRAVEL, ENTERTAINMENT, OTHER
}