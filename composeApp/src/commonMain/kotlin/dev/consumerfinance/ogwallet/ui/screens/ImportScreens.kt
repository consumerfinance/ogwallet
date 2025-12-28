package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.runtime.Composable

/**
 * Wrapper for SMS scanner that delegates to platform-specific implementation
 */
@Composable
expect fun SmsScannerScreenWrapper(onBack: () -> Unit)