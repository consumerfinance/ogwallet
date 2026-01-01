package dev.consumerfinance.ogwallet.data

actual object DataFileReader {
    actual fun readFile(path: String): String {
        // For iOS, file reading would require platform-specific implementation
        // For now, throw exception to fall back to sample data
        throw UnsupportedOperationException("File reading not supported on iOS")
    }
}