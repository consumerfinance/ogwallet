package dev.consumerfinance.ogwallet.util

actual class MboxImporter actual constructor() {
    actual suspend fun pickAndReadMboxFile(): String? {
        val filePath = JvmFilePicker.pickMboxFile() ?: return null
        return try {
            JvmFilePicker.readMboxFile(filePath)
        } catch (e: Exception) {
            null
        }
    }
}

