package dev.consumerfinance.ogwallet

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Initialize Koin for dependency injection
    // This is defined in platform-specific source sets (jsMain/wasmJsMain)
    initKoin()

    ComposeViewport(document.body!!) {
        App()
    }
}

// Expect function to be implemented in platform-specific source sets
expect fun initKoin()