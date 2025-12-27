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
    // More flexible pattern - handles more keywords and formats
    private val spendRegex = Regex(
        "(?i)(?:spent|transaction|debited|debit|purchase|charged|credited|credit|payment|withdrawal|transfer|received|refund)\\s+(?:of\\s+)?(?:rs\\.?|inr|\\$|₹)?\\s*([\\d,.]+).*?(?:at|in|on|for|to|from|via|through)\\s+([A-Za-z0-9\\s&.'/-]+?)(?:\\s+(?:on|at|in|for|via)|\\.|\\b|$)"
    )

    // Alternative pattern for "from card" format
    private val debitFromRegex = Regex(
        "(?i)(?:debited|debit|credited|credit|received|refund)\\s+(?:rs\\.?|inr|\\$|₹)?\\s*([\\d,.]+)\\s+(?:from|to)\\s+(?:card|account|a/c|acct).*?(?:at|on|for|via)\\s+([A-Za-z0-9\\s&.'/-]+?)(?:\\.|\\s|$)"
    )

    // Pattern for amount first, then merchant
    private val amountFirstRegex = Regex(
        "(?i)(?:rs\\.?|inr|\\$|₹)?\\s*([\\d,.]+)\\s+(?:spent|debited|debit|charged|credited|credit|received|refund|payment)\\s+(?:at|on|for|to|from|via)\\s+([A-Za-z0-9\\s&.'/-]+?)(?:\\.|\\s|$)"
    )

    // Regex for: "Card ending in 1234" or "CARD XX1234" or "card 1234"
    private val cardRegex = Regex("(?i)(?:card|acct|a/c|account|ending)\\s*(?:ending|xx|no|num|number)?\\s*(?:in\\s+|with\\s+)?([\\d]{4})")

    suspend fun parse(message: String): RawTransactionMatch? {
        // Try different regex patterns
        var amountMatch = spendRegex.find(message)
        var merchantGroup = 2

        // If that doesn't work, try the "from card" pattern
        if (amountMatch == null) {
            amountMatch = debitFromRegex.find(message)
            merchantGroup = 2
        }

        // If that doesn't work, try amount first pattern
        if (amountMatch == null) {
            amountMatch = amountFirstRegex.find(message)
            merchantGroup = 2
        }

        val cardMatch = cardRegex.find(message)

        return if (amountMatch != null) {
            val amountStr = amountMatch.groupValues[1].replace(",", "").toDouble()
            val merchant = amountMatch.groupValues[merchantGroup].trim()

            // Determine transaction type based on keywords in the message
            val transactionType = when {
                message.contains(Regex("(?i)credited|credit|received|refund")) -> TransactionType.CREDIT
                else -> TransactionType.DEBIT
            }

            RawTransactionMatch(
                amount = amountStr,
                currency = "INR",
                accountHandle = cardMatch?.groupValues?.get(1) ?: "Unknown",
                merchantRaw = merchant,
                transactionType = transactionType,
                rawBody = message
            )
        } else null
    }
}