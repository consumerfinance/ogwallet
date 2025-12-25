package dev.consumerfinance.ogwallet.models.transactions

import androidx.compose.ui.graphics.Color

data class SpendingCategory(
    val name: String,
    val amount: Int,
    val budget: Int,
    val color: Color
)