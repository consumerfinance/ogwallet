package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import dev.consumerfinance.ogwallet.sms.SmsHistoryScanner
import dev.consumerfinance.ogwallet.ui.screens.settings.SmsScanProgress
import dev.consumerfinance.ogwallet.ui.screens.settings.SmsScannerScreen
import kotlinx.coroutines.launch

/**
 * Android-specific wrapper for SmsScannerScreen that handles the actual SMS scanning
 */
@Composable
fun SmsScannerScreenAndroid(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf<SmsScanProgress?>(null) }

    SmsScannerScreen(
        onBack = onBack,
        onStartScan = { daysBack ->
            scope.launch {
                isScanning = true
                scanProgress = null

                val scanner = SmsHistoryScanner(context)
                scanner.scanAllMessages(daysBack).collect { progress ->
                    scanProgress = SmsScanProgress(
                        totalMessages = progress.totalMessages,
                        scannedMessages = progress.scannedMessages,
                        transactionsFound = progress.transactionsFound,
                        transactionsSaved = progress.transactionsSaved,
                        isComplete = progress.isComplete,
                        error = progress.error
                    )

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

