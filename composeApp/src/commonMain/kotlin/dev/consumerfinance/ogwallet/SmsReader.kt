package dev.consumerfinance.ogwallet

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import dev.consumerfinance.ogwallet.ui.screens.settings.SmsScanProgress

expect class SmsReader {
    @Composable
    fun registerSmsReceiver(onSmsReceived: (String) -> Unit)
    fun readSmsMessages(): List<String>
    fun scanSmsMessagesForTransactions(daysBack: Int = 90): Flow<SmsScanProgress>
}
