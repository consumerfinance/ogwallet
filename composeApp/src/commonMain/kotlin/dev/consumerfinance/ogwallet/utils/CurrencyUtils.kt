package dev.consumerfinance.ogwallet.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double, currencyCode: String): String {
    val locale = when (currencyCode) {
        "USD" -> Locale("en", "US")
        "INR" -> Locale("en", "IN")
        else -> Locale.getDefault() // Fallback
    }
    val format = NumberFormat.getCurrencyInstance(locale)
    format.currency = java.util.Currency.getInstance(currencyCode)
    return format.format(amount)
}