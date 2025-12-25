package dev.consumerfinance.ogwallet.models

import androidx.compose.ui.graphics.Color

data class CategorySummary(
    val category: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val color: Color
)

