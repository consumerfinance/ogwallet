package dev.consumerfinance.ogwallet.data

actual object DataFileReader {
    actual fun readFile(path: String): String {
        // For Android, we can't read arbitrary files from the file system
        // In production, data would be fetched from API or bundled as assets
        // For now, throw exception to fall back to sample data
        throw UnsupportedOperationException("File reading not supported on Android")
    }
}