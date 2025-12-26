package dev.consumerfinance.ogwallet.util

import dev.consumerfinance.ogwallet.models.RawTransactionMatch
import kotlinx.datetime.Instant

/**
 * Parser for mbox format email files.
 * Mbox is a standard format for storing email messages.
 */
object MboxParser {
    
    data class Attachment(
        val filename: String,
        val contentType: String,
        val content: String // Placeholder for base64 or raw content
    )

    data class EmailMessage(
        val from: String,
        val subject: String,
        val date: String,
        val body: String,
        val attachments: List<Attachment> = emptyList()
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
        val attachments = mutableListOf<Attachment>()
        var inBody = false
        var inAttachment = false
        var currentAttachmentFilename = "attachment"
        var currentAttachmentContentType = "application/octet-stream"
        val currentAttachmentContent = StringBuilder()

        val attachmentHeaderRegex = Regex("(?i)Content-Disposition:\\s*attachment;\\s*filename\\*?=\"?([^\";]+)\"?")
        val contentTypeRegex = Regex("(?i)Content-Type:\\s*([^;]+)")
        
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
                // Basic attachment detection (highly simplified)
                // This will NOT handle complex MIME structures correctly without a proper MIME parser library
                attachmentHeaderRegex.containsMatchIn(line) -> {
                    val match = attachmentHeaderRegex.find(line)
                    currentAttachmentFilename = match?.groupValues?.get(1) ?: "attachment"
                    inAttachment = true
                    inBody = false // Exit body context
                }
                inAttachment && contentTypeRegex.containsMatchIn(line) -> {
                    val match = contentTypeRegex.find(line)
                    currentAttachmentContentType = match?.groupValues?.get(1) ?: "application/octet-stream"
                }
                inAttachment && line.isBlank() && currentAttachmentContent.isNotEmpty() -> {
                    // Attachment content ended, save it
                    attachments.add(Attachment(currentAttachmentFilename, currentAttachmentContentType, currentAttachmentContent.toString()))
                    currentAttachmentContent.clear()
                    inAttachment = false // Reset
                }
                inAttachment && !line.isBlank() -> {
                    currentAttachmentContent.appendLine(line)
                }
                line.isBlank() && !inBody && !inAttachment -> {
                    inBody = true // Empty line marks start of body (if not in attachment)
                }
                inBody -> {
                    bodyLines.add(line)
                }
            }
        }

        // Add any pending attachment if message ends with one
        if (inAttachment && currentAttachmentContent.isNotEmpty()) {
            attachments.add(Attachment(currentAttachmentFilename, currentAttachmentContentType, currentAttachmentContent.toString()))
        }
        
        return EmailMessage(
            from = from,
            subject = subject,
            date = date,
            body = bodyLines.joinToString("\n"),
            attachments = attachments
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

