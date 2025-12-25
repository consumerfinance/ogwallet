package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * WasmJS implementation - SMS scanning not available on web
 */
@Composable
actual fun SmsScannerScreenWrapper(onBack: () -> Unit) {
    Text("SMS scanning is only available on Android")
}

