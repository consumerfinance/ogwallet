package dev.consumerfinance.ogwallet.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double, currencyCode: String): String {
    return try {
        val locale = when (currencyCode) {
            "USD" -> Locale("en", "US")
            "INR" -> Locale("en", "IN")
            else -> Locale.getDefault() // Fallback
        }
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = java.util.Currency.getInstance(currencyCode)
        format.format(amount)
    } catch (e: Exception) {
        // Fallback to simple formatting if currency formatting fails
        "${currencyCode.uppercase()} ${String.format("%.2f", amount)}"
    }
}