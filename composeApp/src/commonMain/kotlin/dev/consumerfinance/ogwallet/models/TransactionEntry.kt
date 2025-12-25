package dev.consumerfinance.ogwallet.models

import kotlinx.datetime.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
data class TransactionEntry(
    val id: Long,
    val amount: Double,
    val merchant: String,
    val category: String,
    val timestamp: Instant,
    val cardHandle: String
)

