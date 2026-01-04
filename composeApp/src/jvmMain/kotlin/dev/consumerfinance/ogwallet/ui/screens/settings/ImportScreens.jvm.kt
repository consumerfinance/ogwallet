package dev.consumerfinance.ogwallet.ui.screens.settings

import androidx.compose.runtime.*
import dev.consumerfinance.ogwallet.SmsReader
import kotlinx.coroutines.launch

/**
 * JVM implementation of SMS scanner wrapper
 */
@Composable
actual fun SmsScannerScreenWrapper(onBack: () -> Unit) {
    val smsReader = remember { SmsReader() }
    var scanProgress by remember { mutableStateOf<SmsScanProgress?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    SmsScannerScreen(
        onBack = onBack,
        onStartScan = { daysBack ->
            isScanning = true
            scope.launch {
                smsReader.scanSmsMessagesForTransactions(daysBack).collect { progress ->
                    scanProgress = progress
                    if (progress.isComplete) {
                        isScanning = false
                    }
                }
            }
        },
        scanProgress = scanProgress,
        isScanning = isScanning
    )
}