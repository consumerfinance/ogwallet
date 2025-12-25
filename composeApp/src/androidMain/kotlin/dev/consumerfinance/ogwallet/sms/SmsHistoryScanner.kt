package dev.consumerfinance.ogwallet.sms

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.models.TransactionEntry
import dev.consumerfinance.ogwallet.util.SmsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Scans all SMS messages on the device and extracts transaction data
 */
class SmsHistoryScanner(private val context: Context) : KoinComponent {
    private val repository: TransactionRepository by inject()
    
    companion object {
        private const val TAG = "SmsHistoryScanner"
        
        // Common bank sender IDs in India
        private val BANK_SENDER_IDS = setOf(
            "HDFCBK", "ICICIB", "SBIIN", "AXISBK", "KOTAKB",
            "YESBNK", "INDUSB", "PNBSMS", "BOISMS", "CBSSBI",
            "SCBANK", "CITIBK", "HSBCIN", "DEUTSC", "RBLBNK",
            "IDFCFB", "AUBANK", "FEDRAL", "KRVYSB", "UNIONB",
            "VM-HDFCBK", "VM-ICICIB", "VM-SBIIN", "VM-AXISBK",
            "AX-HDFCBK", "AX-ICICIB", "AX-SBIIN", "AX-AXISBK"
        )
    }
    
    data class ScanProgress(
        val totalMessages: Int,
        val scannedMessages: Int,
        val transactionsFound: Int,
        val transactionsSaved: Int,
        val isComplete: Boolean = false,
        val error: String? = null
    )
    
    /**
     * Scans all SMS messages and returns a flow of progress updates
     */
    fun scanAllMessages(daysBack: Int = 90): Flow<ScanProgress> = flow {
        try {
            Log.d(TAG, "Starting SMS scan for last $daysBack days")
            
            val messages = getAllSmsMessages(daysBack)
            val totalMessages = messages.size
            var scannedMessages = 0
            var transactionsFound = 0
            var transactionsSaved = 0
            
            emit(ScanProgress(totalMessages, 0, 0, 0))
            
            for (sms in messages) {
                scannedMessages++
                
                // Try to parse the SMS
                val match = SmsParser.parse(sms.body)
                
                if (match != null) {
                    transactionsFound++
                    Log.d(TAG, "Found transaction: ${match.amount} at ${match.merchantRaw}")
                    
                    // Save to database
                    try {
                        repository.addTransaction(
                            TransactionEntry(
                                id = 0,
                                amount = match.amount,
                                merchant = match.merchantRaw,
                                category = "OTHER", // Default category
                                cardHandle = match.accountHandle,
                                timestamp = sms.timestamp
                            )
                        )
                        transactionsSaved++
                        Log.d(TAG, "Transaction saved successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to save transaction: ${e.message}", e)
                    }
                }
                
                // Emit progress every 10 messages or at the end
                if (scannedMessages % 10 == 0 || scannedMessages == totalMessages) {
                    emit(ScanProgress(
                        totalMessages = totalMessages,
                        scannedMessages = scannedMessages,
                        transactionsFound = transactionsFound,
                        transactionsSaved = transactionsSaved
                    ))
                }
            }
            
            // Emit final completion
            emit(ScanProgress(
                totalMessages = totalMessages,
                scannedMessages = scannedMessages,
                transactionsFound = transactionsFound,
                transactionsSaved = transactionsSaved,
                isComplete = true
            ))
            
            Log.d(TAG, "SMS scan complete. Found $transactionsFound transactions, saved $transactionsSaved")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning SMS: ${e.message}", e)
            emit(ScanProgress(0, 0, 0, 0, isComplete = true, error = e.message))
        }
    }
    
    /**
     * Retrieves all SMS messages from the device
     */
    private suspend fun getAllSmsMessages(daysBack: Int): List<SmsMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessage>()
        
        try {
            val uri = Telephony.Sms.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            )
            
            // Calculate timestamp for filtering (daysBack from now)
            val cutoffTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
            val selection = "${Telephony.Sms.DATE} >= ? AND ${Telephony.Sms.TYPE} = ?"
            val selectionArgs = arrayOf(
                cutoffTime.toString(),
                Telephony.Sms.MESSAGE_TYPE_INBOX.toString()
            )
            
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${Telephony.Sms.DATE} DESC"
            )
            
            cursor?.use {
                val idIndex = it.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                
                while (it.moveToNext()) {
                    val address = it.getString(addressIndex) ?: ""
                    val body = it.getString(bodyIndex) ?: ""
                    
                    // Filter by bank sender IDs for efficiency
                    if (isBankMessage(address, body)) {
                        messages.add(
                            SmsMessage(
                                id = it.getLong(idIndex),
                                address = address,
                                body = body,
                                timestamp = Instant.fromEpochMilliseconds(it.getLong(dateIndex))
                            )
                        )
                    }
                }
            }
            
            Log.d(TAG, "Retrieved ${messages.size} bank SMS messages from last $daysBack days")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS: ${e.message}", e)
        }
        
        return@withContext messages
    }

    /**
     * Checks if an SMS is likely from a bank based on sender ID or content
     */
    private fun isBankMessage(address: String, body: String): Boolean {
        // Check if sender ID matches known bank IDs
        val normalizedAddress = address.uppercase().replace("-", "")
        if (BANK_SENDER_IDS.any { normalizedAddress.contains(it) }) {
            return true
        }

        // Check for common transaction keywords in the message
        val transactionKeywords = listOf(
            "spent", "debited", "debit", "transaction", "purchase",
            "credited", "credit", "card", "account", "balance",
            "rs.", "inr", "₹", "upi", "payment"
        )

        val lowerBody = body.lowercase()
        return transactionKeywords.any { lowerBody.contains(it) }
    }

    data class SmsMessage(
        val id: Long,
        val address: String,
        val body: String,
        val timestamp: Instant
    )
}

