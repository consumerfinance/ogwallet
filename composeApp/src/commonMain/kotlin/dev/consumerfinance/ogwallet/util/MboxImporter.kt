package dev.consumerfinance.ogwallet.util

/**
 * Platform-agnostic interface for importing mbox files
 */
expect class MboxImporter() {
    /**
     * Pick and read an mbox file from the file system
     * Returns the file content as a string, or null if cancelled
     */
    suspend fun pickAndReadMboxFile(): String?
}

/**
 * Result of mbox import operation
 */
data class MboxImportResult(
    val totalMessages: Int,
    val importedTransactions: Int,
    val failedTransactions: Int,
    val totalAmount: Double,
    val errors: List<String> = emptyList()
)

