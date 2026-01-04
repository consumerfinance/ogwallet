package dev.consumerfinance.ogwallet.ui.screens.settings

import androidx.compose.runtime.Composable

/**
 * WASM JS implementation of SMS scanner wrapper - not supported
 */
@Composable
actual fun SmsScannerScreenWrapper(onBack: () -> Unit) {
    // Not supported on WASM JS
}
