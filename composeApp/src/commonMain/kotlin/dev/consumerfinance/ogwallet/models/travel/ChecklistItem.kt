package dev.consumerfinance.ogwallet.models.travel

data class ChecklistItem(
    val id: String,
    val text: String,
    val completed: Boolean,
    val category: String
)