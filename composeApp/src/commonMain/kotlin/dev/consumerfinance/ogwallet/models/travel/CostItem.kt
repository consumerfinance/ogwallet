package dev.consumerfinance.ogwallet.models.travel

data class CostItem(
    val id: String,
    val category: CostCategory,
    val description: String,
    val amount: Double
)