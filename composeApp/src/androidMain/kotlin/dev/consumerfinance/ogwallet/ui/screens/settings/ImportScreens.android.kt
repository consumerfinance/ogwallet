package dev.consumerfinance.ogwallet.ui.screens.settings

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import dev.consumerfinance.ogwallet.SmsReader
import kotlinx.coroutines.launch

/**
 * Android implementation of SMS scanner wrapper
 */
@Composable
actual fun SmsScannerScreenWrapper(onBack: () -> Unit) {
    val context = LocalContext.current
    val smsReader = remember { SmsReader(context) }
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