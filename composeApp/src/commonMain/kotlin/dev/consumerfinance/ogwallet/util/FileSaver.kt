package dev.consumerfinance.ogwallet.util

expect class FileSaver {
    suspend fun saveFile(filename: String, content: String, mimeType: String): Boolean
}
