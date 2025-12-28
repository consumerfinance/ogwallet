package dev.consumerfinance.ogwallet.util

import dev.consumerfinance.ogwallet.models.RawTransactionMatch
import dev.consumerfinance.ogwallet.models.TransactionType

/**
 * Parser for extracting transaction information from email content.
 * Handles various email formats from banks and payment processors.
 */
object EmailParser {
    
    // Pattern for transaction emails with amount and merchant
    private val transactionRegex = Regex(
        "(?i)(?:transaction|payment|purchase|spent|debited|charged|credited).*?(?:amount|value)?\\s*(?:rs\\.?|inr|usd|\\$|₹)?\\s*([\\d,.]+).*?(?:at|to|from|merchant|for|of|on)\\s+([A-Za-z0-9\\s&.'-]+)(?:\\.|\\n|$)",
        RegexOption.DOT_MATCHES_ALL
    )
    
    // Pattern for bill payment emails
    private val billRegex = Regex(
        "(?i)(?:bill|invoice|statement|paid for|payment for).*?(?:amount|total|due).*?(?:rs\\.?|inr|usd|\\$|₹)?\\s*([\\d,.]+)",
        RegexOption.DOT_MATCHES_ALL
    )
    
    // Pattern for credit card payment confirmation
    private val paymentRegex = Regex(
        "(?i)(?:payment|paid).*?(?:rs\\.?|inr|usd|\\$|₹)\\s*([\\d,.]+).*?(?:card|account).*?([\\d]{4})",
        RegexOption.DOT_MATCHES_ALL
    )
    
    // Card number extraction
    private val cardRegex = Regex("(?i)(?:card|acct|account).*?(?:ending|xx|\\*{4}|no)\\s*([\\d]{4})")
    
    // Date extraction (various formats)
    private val dateRegex = Regex("(?i)(?:date|on)\\s*(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}|\\d{4}[-/]\\d{1,2}[-/]\\d{1,2})")

    // Category mapping based on merchant keywords
    private fun categorizeMerchant(merchant: String, message: String): String {
        val combinedText = "$merchant $message".lowercase()

        return when {
            // Food & Dining
            combinedText.contains("restaurant") || combinedText.contains("cafe") || combinedText.contains("coffee") ||
            combinedText.contains("starbucks") || combinedText.contains("dominos") || combinedText.contains("pizza") ||
            combinedText.contains("burger") || combinedText.contains("food") || combinedText.contains("dining") ||
            combinedText.contains("swiggy") || combinedText.contains("zomato") || combinedText.contains("uber eats") -> "FOOD"
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
            combinedText.contains("spotify") -> "ENTERTAINMENT"
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

    /**
     * Parse email content to extract transaction information
     */
    fun parse(emailBody: String, subject: String = ""): RawTransactionMatch? {
        val fullContent = "$subject\n$emailBody"
        
        // Try transaction pattern first
        var match = transactionRegex.find(fullContent)
        if (match != null) {
            return createMatch(match.groupValues[1], match.groupValues[2], fullContent, TransactionType.DEBIT)
        }
        
        // Try payment pattern
        match = paymentRegex.find(fullContent)
        if (match != null) {
            val cardNumber = match.groupValues.getOrNull(2) ?: "Unknown"
            return createMatch(match.groupValues[1], "Payment", fullContent, TransactionType.CREDIT, cardNumber)
        }
        
        // Try bill pattern
        match = billRegex.find(fullContent)
        if (match != null) {
            return createMatch(match.groupValues[1], "Bill Payment", fullContent, TransactionType.DEBIT)
        }
        
        return null
    }
    
    private fun createMatch(
        amountStr: String,
        merchant: String,
        rawBody: String,
        type: TransactionType,
        cardHandle: String? = null
    ): RawTransactionMatch {
        val amount = amountStr.replace(",", "").toDoubleOrNull() ?: 0.0
        val card = cardHandle ?: extractCardNumber(rawBody)
        val category = categorizeMerchant(merchant, rawBody)

        return RawTransactionMatch(
            amount = amount,
            currency = detectCurrency(rawBody),
            accountHandle = card,
            merchantRaw = merchant.trim(),
            category = category,
            transactionType = type,
            rawBody = rawBody
        )
    }
    
    private fun extractCardNumber(content: String): String {
        val match = cardRegex.find(content)
        return match?.groupValues?.get(1) ?: "Unknown"
    }
    
    private fun detectCurrency(content: String): String {
        return when {
            content.contains("₹", ignoreCase = true) || 
            content.contains("INR", ignoreCase = true) || 
            content.contains("Rs.", ignoreCase = true) -> "INR"
            content.contains("$") || content.contains("USD", ignoreCase = true) -> "USD"
            content.contains("€") || content.contains("EUR", ignoreCase = true) -> "EUR"
            else -> "INR" // Default
        }
    }
}

