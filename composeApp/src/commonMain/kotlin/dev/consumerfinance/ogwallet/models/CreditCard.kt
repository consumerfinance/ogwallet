package dev.consumerfinance.ogwallet.models

import androidx.compose.ui.graphics.Color

data class CreditCard(
    val id: String,
    val name: String,
    val cardNumber: String,
    val last4: String,
    val cvv: String,
    val expiry: String,
    val balance: Double,
    val limit: Int,
    val availableCredit: Double,
    val gradient: List<Color>,
    val network: String,
    val nextPayment: String?,
    val minPayment: Double
)