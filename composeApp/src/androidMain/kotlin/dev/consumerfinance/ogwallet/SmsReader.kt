package dev.consumerfinance.ogwallet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.compose.runtime.Composable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import dev.consumerfinance.ogwallet.ui.screens.SmsScanProgress
import dev.consumerfinance.ogwallet.sms.SmsHistoryScanner

actual class SmsReader(private val context: Context) {

    @Composable
    actual fun registerSmsReceiver(onSmsReceived: (String) -> Unit) {
        // Implementation for registering a broadcast receiver can be added here
    }

    actual fun readSmsMessages(): List<String> {
        if (context.checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }
        val messages = mutableListOf<String>()
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY),
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                do {
                    messages.add(it.getString(bodyIndex))
                } while (it.moveToNext())
            }
        }
        return messages
    }

    actual fun scanSmsMessagesForTransactions(daysBack: Int): Flow<SmsScanProgress> = flow {
        val scanner = SmsHistoryScanner(context)
        scanner.scanAllMessages(daysBack).collect { progress ->
            emit(SmsScanProgress(
                totalMessages = progress.totalMessages,
                scannedMessages = progress.scannedMessages,
                transactionsFound = progress.transactionsFound,
                transactionsSaved = progress.transactionsSaved,
                isComplete = progress.isComplete,
                error = progress.error
            ))
        }
    }
}
