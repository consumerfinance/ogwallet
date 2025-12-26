package dev.consumerfinance.ogwallet.util

// Helper function for formatting currency amounts
fun formatCurrency(amount: Double, decimals: Int = 2): String {
    val rounded = (amount * 100).toLong() / 100.0
    val intPart = rounded.toLong()
    val decimalPart = ((rounded - intPart) * 100).toLong()
    return if (decimals == 0) {
        "$intPart"
    } else {
        "$intPart.${decimalPart.toString().padStart(2, '0')}"
    }
}