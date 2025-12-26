package dev.consumerfinance.ogwallet

import androidx.compose.runtime.Composable

expect class SmsReader {
    @Composable
    fun registerSmsReceiver(onSmsReceived: (String) -> Unit)
    fun readSmsMessages(): List<String>
}
