package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.runtime.Composable

/**
 * Android implementation of SMS scanner wrapper
 */
@Composable
actual fun SmsScannerScreenWrapper(onBack: () -> Unit) {
    SmsScannerScreenAndroid(onBack = onBack)
}

