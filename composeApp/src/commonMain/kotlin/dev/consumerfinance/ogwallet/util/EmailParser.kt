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
        "(?i)(?:transaction|payment|purchase|spent|debited|charged|credited).*?(?:amount|value)?\\s*(?:rs\\.?|inr|usd|\\$|₹)?\\s*([\\d,.]+).*?(?:at|to|from|merchant|for|of)\\s+([A-Za-z0-9\\s&.'-]+)(?:\\.|\\n|$)",
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
        
        return RawTransactionMatch(
            amount = amount,
            currency = detectCurrency(rawBody),
            accountHandle = card,
            merchantRaw = merchant.trim(),
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

