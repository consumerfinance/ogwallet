
package dev.consumerfinance.ogwallet.util

import dev.consumerfinance.ogwallet.db.TransactionRepository

import dev.consumerfinance.ogwallet.models.RawTransactionMatch
import dev.consumerfinance.ogwallet.models.TransactionType

// shared/src/commonMain/kotlin/dev/consumerfinance/ogwallet/util/SmsParser.kt

class SmsParser(
    private val transactionRepository: TransactionRepository? = null
) {
    // Regex patterns for different SMS formats
    // Pattern 1: "Spent Rs.500.00 on CARD XX1234 at AMAZON"
    // Pattern 2: "Transaction of Rs.1500.50 on card ending 5678 at STARBUCKS"
    // Pattern 3: "Debited Rs.2500.00 from card XX9876 at FLIPKART"
    // More flexible pattern - handles more keywords and formats
    private val spendRegex = Regex(
        "(?i)(?:spent|transaction|debited|debit|purchase|charged|credited|credit|payment|withdrawal|transfer|received|refund|sent)\\s+(?:of\\s+)?(?:rs\\.?|inr|\\$|₹)?\\s*([\\d,.]+).*?(?:at|in|on|for|to|from|via|through)\\s+([A-Za-z0-9\\s&.'/-]+?)(?:\\s+(?:on|at|in|for|via)|\\.|\\b|$)"
    )

    // Alternative pattern for "from card" format
    private val debitFromRegex = Regex(
        "(?i)(?:debited|debit|credited|credit|received|refund|sent)\\s+(?:rs\\.?|inr|\\$|₹)?\\s*([\\d,.]+)\\s+(?:from|to)\\s+(?:card|account|a/c|acct).*?(?:at|on|for|via)\\s+([A-Za-z0-9\\s&.'/-]+?)(?:\\.|\\s|$)"
    )

    // Pattern for amount first, then merchant
    private val amountFirstRegex = Regex(
        "(?i)(?:rs\\.?|inr|\\$|₹)?\\s*([\\d,.]+)\\s+(?:spent|debited|debit|charged|credited|credit|received|refund|payment|sent)\\s+(?:at|on|for|to|from|via)\\s+([A-Za-z0-9\\s&.'/-]+?)(?:\\.|\\s|$)"
    )

    // Regex for: "Card ending in 1234" or "CARD XX1234" or "card 1234"
    private val cardRegex = Regex("(?i)(?:card|acct|a/c|account|ending)\\s*(?:ending|xx|no|num|number)?\\s*(?:in\\s+|with\\s+)?([\\d]{4})")

    // Category mapping based on merchant keywords
    private fun categorizeMerchant(merchant: String, message: String): String {
        val combinedText = "$merchant $message".lowercase()

        return when {
            // Food & Dining
            combinedText.contains("restaurant") || combinedText.contains("cafe") || combinedText.contains("coffee") ||
            combinedText.contains("starbucks") || combinedText.contains("dominos") || combinedText.contains("pizza") ||
            combinedText.contains("burger") || combinedText.contains("food") || combinedText.contains("dining") ||
            combinedText.contains("swiggy") || combinedText.contains("zomato") || combinedText.contains("uber eats") ||
            combinedText.contains("hungry") -> "FOOD"
            // Transportation
            combinedText.contains("uber") || combinedText.contains("ola") || combinedText.contains("taxi") ||
            combinedText.contains("cab") || combinedText.contains("fuel") || combinedText.contains("petrol") ||
            combinedText.contains("diesel") || combinedText.contains("gas") || combinedText.contains("bpcl") ||
            combinedText.contains("hpcl") || combinedText.contains("iocl") || combinedText.contains("transport") ||
            combinedText.contains("auto") || combinedText.contains("rickshaw") || combinedText.contains("metro") ||
            combinedText.contains("bus") || combinedText.contains("train") || combinedText.contains("flight") ||
            combinedText.contains("airline") || combinedText.contains("indigo") || combinedText.contains("air india") -> "TRANSPORT"
            // Shopping
            combinedText.contains("amazon") || combinedText.contains("flipkart") || combinedText.contains("myntra") ||
            combinedText.contains("ajio") || combinedText.contains("bigbasket") || combinedText.contains("grofers") ||
            combinedText.contains("shopping") || combinedText.contains("store") || combinedText.contains("mall") ||
            combinedText.contains("market") || combinedText.contains("supermarket") -> "SHOPPING"
            // Entertainment
            combinedText.contains("netflix") || combinedText.contains("prime") || combinedText.contains("hotstar") ||
            combinedText.contains("zee5") || combinedText.contains("altbalaji") || combinedText.contains("movie") ||
            combinedText.contains("cinema") || combinedText.contains("theatre") || combinedText.contains("bookmyshow") ||
            combinedText.contains("entertainment") || combinedText.contains("game") || combinedText.contains("gaming") ||
            combinedText.contains("spotify") || combinedText.contains("apple") -> "ENTERTAINMENT"
            // Bills & Utilities
            combinedText.contains("electricity") || combinedText.contains("water") || combinedText.contains("gas") ||
            combinedText.contains("internet") || combinedText.contains("phone") || combinedText.contains("mobile") ||
            combinedText.contains("broadband") || combinedText.contains("bill") || combinedText.contains("utility") ||
            combinedText.contains("recharge") || combinedText.contains("jio") || combinedText.contains("airtel") ||
            combinedText.contains("voda") || combinedText.contains("idea") -> "BILLS"
            // Health & Medical
            combinedText.contains("hospital") || combinedText.contains("doctor") || combinedText.contains("pharmacy") ||
            combinedText.contains("medical") || combinedText.contains("health") || combinedText.contains("apollo") ||
            combinedText.contains("max") || combinedText.contains("medanta") || combinedText.contains("clinic") -> "HEALTH"
            // Travel
            combinedText.contains("hotel") || combinedText.contains("booking") || combinedText.contains("oyo") ||
            combinedText.contains("makemytrip") || combinedText.contains("goibibo") || combinedText.contains("travel") ||
            combinedText.contains("vacation") || combinedText.contains("trip") || combinedText.contains("tour") -> "TRAVEL"
            // Education
            combinedText.contains("school") || combinedText.contains("college") || combinedText.contains("university") ||
            combinedText.contains("course") || combinedText.contains("education") || combinedText.contains("book") ||
            combinedText.contains("stationery") || combinedText.contains("byjus") || combinedText.contains("vedantu") -> "EDUCATION"
            // Groceries
            combinedText.contains("grocery") || combinedText.contains("grocers") || combinedText.contains("vegetable") ||
            combinedText.contains("fruit") || combinedText.contains("milk") || combinedText.contains("bread") ||
            combinedText.contains("supermarket") || combinedText.contains("bigbasket") || combinedText.contains("dmart") ||
            combinedText.contains("reliance fresh") -> "GROCERIES"
            // Default
            else -> "OTHER"
        }
    }

    fun parse(message: String): RawTransactionMatch? {
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

            // Categorize the transaction based on merchant and message content
            val category = categorizeMerchant(merchant, message)

            RawTransactionMatch(
                amount = amountStr,
                currency = "INR",
                accountHandle = cardMatch?.groupValues?.get(1) ?: "Unknown",
                merchantRaw = merchant,
                category = category,
                transactionType = transactionType,
                rawBody = message
            )
        } else null
    }
}