package dev.consumerfinance.ogwallet

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.consumerfinance.ogwallet.di.initKoin

fun main() = application {
    // Initialize Koin for dependency injection
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "OGWallet - Desktop",
        state = rememberWindowState(width = 1400.dp, height = 900.dp)
    ) {
        App()
    }
}