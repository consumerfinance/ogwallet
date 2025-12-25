package dev.consumerfinance.ogwallet.util

import dev.consumerfinance.ogwallet.models.RawTransactionMatch
import kotlinx.datetime.Instant

/**
 * Parser for mbox format email files.
 * Mbox is a standard format for storing email messages.
 */
object MboxParser {
    
    data class EmailMessage(
        val from: String,
        val subject: String,
        val date: String,
        val body: String
    )
    
    /**
     * Parse an mbox file content and extract all email messages
     */
    fun parseMessages(mboxContent: String): List<EmailMessage> {
        val messages = mutableListOf<EmailMessage>()
        
        // Split by "From " at the beginning of lines (mbox message separator)
        val messageParts = mboxContent.split(Regex("(?m)^From "))
        
        for (part in messageParts) {
            if (part.isBlank()) continue
            
            val message = parseMessage(part)
            if (message != null) {
                messages.add(message)
            }
        }
        
        return messages
    }
    
    /**
     * Parse a single email message from mbox format
     */
    private fun parseMessage(messageContent: String): EmailMessage? {
        if (messageContent.isBlank()) return null
        
        val lines = messageContent.lines()
        var from = ""
        var subject = ""
        var date = ""
        val bodyLines = mutableListOf<String>()
        var inBody = false
        
        for (line in lines) {
            when {
                line.startsWith("From:", ignoreCase = true) -> {
                    from = line.substringAfter(":", "").trim()
                }
                line.startsWith("Subject:", ignoreCase = true) -> {
                    subject = line.substringAfter(":", "").trim()
                }
                line.startsWith("Date:", ignoreCase = true) -> {
                    date = line.substringAfter(":", "").trim()
                }
                line.isBlank() && !inBody -> {
                    inBody = true // Empty line marks start of body
                }
                inBody -> {
                    bodyLines.add(line)
                }
            }
        }
        
        return EmailMessage(
            from = from,
            subject = subject,
            date = date,
            body = bodyLines.joinToString("\n")
        )
    }
    
    /**
     * Extract transactions from mbox file
     */
    fun extractTransactions(mboxContent: String): List<RawTransactionMatch> {
        val messages = parseMessages(mboxContent)
        val transactions = mutableListOf<RawTransactionMatch>()
        
        for (message in messages) {
            // Filter for bank/payment related emails
            if (isTransactionEmail(message)) {
                val transaction = EmailParser.parse(message.body, message.subject)
                if (transaction != null) {
                    transactions.add(transaction)
                }
            }
        }
        
        return transactions
    }
    
    /**
     * Check if an email is likely to contain transaction information
     */
    private fun isTransactionEmail(message: EmailMessage): Boolean {
        val keywords = listOf(
            "transaction", "payment", "purchase", "bill", "invoice",
            "statement", "charged", "debited", "credited", "spent",
            "bank", "card", "account", "receipt"
        )
        
        val searchText = "${message.from} ${message.subject} ${message.body}".lowercase()
        
        return keywords.any { keyword -> searchText.contains(keyword) }
    }
    
    /**
     * Get summary statistics from mbox file
     */
    fun getSummary(mboxContent: String): MboxSummary {
        val messages = parseMessages(mboxContent)
        val transactions = extractTransactions(mboxContent)
        
        return MboxSummary(
            totalMessages = messages.size,
            transactionMessages = transactions.size,
            totalAmount = transactions.sumOf { it.amount }
        )
    }
    
    data class MboxSummary(
        val totalMessages: Int,
        val transactionMessages: Int,
        val totalAmount: Double
    )
}

