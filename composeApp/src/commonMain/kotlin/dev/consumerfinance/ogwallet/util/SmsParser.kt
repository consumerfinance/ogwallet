package dev.consumerfinance.ogwallet.util

import dev.consumerfinance.ogwallet.db.TransactionRepository

import dev.consumerfinance.ogwallet.models.RawTransactionMatch
import dev.consumerfinance.ogwallet.models.TransactionType

// shared/src/commonMain/kotlin/dev/consumerfinance/ogwallet/util/SmsParser.kt

class SmsParser(
    private val transactionRepository: TransactionRepository
) {
    // Regex patterns for different SMS formats
    // Pattern 1: "Spent Rs.500.00 on CARD XX1234 at AMAZON"
    // Pattern 2: "Transaction of Rs.1500.50 on card ending 5678 at STARBUCKS"
    // Pattern 3: "Debited Rs.2500.00 from card XX9876 at FLIPKART"
    // More flexible pattern
    private val spendRegex = Regex(
        "(?i)(?:spent|transaction|debited|debit|purchase|charged|credited|credit)\\s+(?:of\\s+)?(?:rs\\.?|inr|\\$|₹)?\\s*([\\d,.]+).*?(?:at|in|on|for|to|from)\\s+([A-Za-z0-9\\s&.'-]+?)(?:\\s+(?:on|at|in)|\\.|\\b|$)"
    )

    // Alternative pattern for "from card" format
    private val debitFromRegex = Regex(
        "(?i)(?:debited|debit|credited|credit)\\s+(?:rs\\.?|inr|\\$|₹)?\\s*([\\d,.]+)\\s+(?:from|to)\\s+(?:card|account).*?(?:at|on|for)\\s+([A-Z][A-Za-z0-9\\s&]+?)(?:\\.|$)"
    )

    // Regex for: "Card ending in 1234" or "CARD XX1234" or "card 1234"
    private val cardRegex = Regex("(?i)(?:card|acct|a/c|account)\\s*(?:ending|xx|no|num|number)?\\s*(?:in\\s+)?([\\d]{4})")

    suspend fun parse(message: String): RawTransactionMatch? {
        // Try the main spend regex first
        var amountMatch = spendRegex.find(message)

        // If that doesn't work, try the "from card" pattern
        if (amountMatch == null) {
            amountMatch = debitFromRegex.find(message)
        }

        val cardMatch = cardRegex.find(message)

        return if (amountMatch != null) {
            RawTransactionMatch(
                amount = amountMatch.groupValues[1].replace(",", "").toDouble(),
                currency = "INR",
                accountHandle = cardMatch?.groupValues?.get(1) ?: "Unknown",
                merchantRaw = amountMatch.groupValues[2].trim(),
                transactionType = TransactionType.DEBIT,
                rawBody = message
            )
        } else null
    }
}