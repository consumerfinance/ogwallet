package dev.consumerfinance.ogwallet

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import dev.consumerfinance.ogwallet.ui.screens.settings.SmsScanProgress

actual class SmsReader {

    @Composable
    actual fun registerSmsReceiver(onSmsReceived: (String) -> Unit) {
        // Not supported on WASM
    }

    actual fun readSmsMessages(): List<String> = emptyList()

    actual fun scanSmsMessagesForTransactions(daysBack: Int): Flow<SmsScanProgress> = flowOf(SmsScanProgress(0, 0, 0, 0, true))

}