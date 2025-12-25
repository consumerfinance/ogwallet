package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * iOS implementation - SMS scanning not available on iOS
 */
@Composable
actual fun SmsScannerScreenWrapper(onBack: () -> Unit) {
    Text("SMS scanning is not available on iOS")
}

