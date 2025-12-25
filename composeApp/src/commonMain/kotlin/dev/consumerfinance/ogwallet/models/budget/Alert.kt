package dev.consumerfinance.ogwallet.models.budget

data class Alert(
    val id: Int,
    val category: String,
    val message: String,
    val severity: String
)