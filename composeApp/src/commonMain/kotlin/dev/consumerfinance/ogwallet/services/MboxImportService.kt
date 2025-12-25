package dev.consumerfinance.ogwallet.services

import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.models.TransactionEntry
import dev.consumerfinance.ogwallet.util.MboxImporter
import dev.consumerfinance.ogwallet.util.MboxImportResult
import dev.consumerfinance.ogwallet.util.MboxParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Service for importing transactions from mbox email files.
 * Handles the complete workflow: file selection, parsing, validation, and database import.
 */
class MboxImportService(
    private val repository: TransactionRepository,
    private val mboxImporter: MboxImporter = MboxImporter()
) {
    
    /**
     * Import transactions from an mbox file.
     * Returns detailed import results including success/failure counts.
     */
    suspend fun importFromMbox(): MboxImportResult = withContext(Dispatchers.Default) {
        try {
            // Step 1: Pick and read mbox file
            val mboxContent = mboxImporter.pickAndReadMboxFile()
                ?: return@withContext MboxImportResult(
                    totalMessages = 0,
                    importedTransactions = 0,
                    failedTransactions = 0,
                    totalAmount = 0.0,
                    errors = listOf("No file selected or import cancelled")
                )
            
            // Step 2: Parse mbox file
            val messages = MboxParser.parseMessages(mboxContent)
            val transactions = MboxParser.extractTransactions(mboxContent)
            
            if (transactions.isEmpty()) {
                return@withContext MboxImportResult(
                    totalMessages = messages.size,
                    importedTransactions = 0,
                    failedTransactions = 0,
                    totalAmount = 0.0,
                    errors = listOf("No transactions found in the mbox file")
                )
            }
            
            // Step 3: Import transactions to database
            var successCount = 0
            var failureCount = 0
            val errors = mutableListOf<String>()
            val currentTime = Clock.System.now()
            
            transactions.forEach { match ->
                try {
                    repository.addTransaction(
                        TransactionEntry(
                            id = 0,
                            amount = match.amount,
                            merchant = match.merchantRaw,
                            category = categorizeTransaction(match.merchantRaw),
                            cardHandle = match.accountHandle,
                            timestamp = currentTime
                        )
                    )
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    errors.add("Failed to import transaction: ${match.merchantRaw} - ${e.message}")
                }
            }
            
            // Step 4: Return results
            MboxImportResult(
                totalMessages = messages.size,
                importedTransactions = successCount,
                failedTransactions = failureCount,
                totalAmount = transactions.sumOf { it.amount },
                errors = errors
            )
            
        } catch (e: Exception) {
            MboxImportResult(
                totalMessages = 0,
                importedTransactions = 0,
                failedTransactions = 0,
                totalAmount = 0.0,
                errors = listOf("Import failed: ${e.message}")
            )
        }
    }
    
    /**
     * Preview transactions from an mbox file without importing.
     * Useful for showing users what will be imported before committing.
     */
    suspend fun previewMboxTransactions(): MboxParser.MboxSummary? = withContext(Dispatchers.Default) {
        try {
            val mboxContent = mboxImporter.pickAndReadMboxFile() ?: return@withContext null
            MboxParser.getSummary(mboxContent)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Simple category detection based on merchant name.
     * Can be enhanced with more sophisticated categorization logic.
     */
    private fun categorizeTransaction(merchant: String): String {
        val merchantLower = merchant.lowercase()
        
        return when {
            merchantLower.contains("amazon") || merchantLower.contains("flipkart") || 
            merchantLower.contains("myntra") -> "SHOPPING"
            
            merchantLower.contains("swiggy") || merchantLower.contains("zomato") || 
            merchantLower.contains("uber eats") -> "FOOD"
            
            merchantLower.contains("uber") || merchantLower.contains("ola") || 
            merchantLower.contains("rapido") -> "TRANSPORT"
            
            merchantLower.contains("netflix") || merchantLower.contains("prime") || 
            merchantLower.contains("spotify") || merchantLower.contains("hotstar") -> "ENTERTAINMENT"
            
            merchantLower.contains("electricity") || merchantLower.contains("water") || 
            merchantLower.contains("gas") || merchantLower.contains("bill") -> "UTILITIES"
            
            merchantLower.contains("pharmacy") || merchantLower.contains("hospital") || 
            merchantLower.contains("clinic") -> "HEALTHCARE"
            
            merchantLower.contains("fuel") || merchantLower.contains("petrol") || 
            merchantLower.contains("diesel") -> "FUEL"
            
            else -> "OTHER"
        }
    }
}

