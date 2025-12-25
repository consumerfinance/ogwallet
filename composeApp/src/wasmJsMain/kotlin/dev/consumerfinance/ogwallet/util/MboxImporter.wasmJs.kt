package dev.consumerfinance.ogwallet.util

actual class MboxImporter actual constructor() {
    actual suspend fun pickAndReadMboxFile(): String? {
        return WebFilePicker.pickAndReadMboxFile()
    }
}

