package dev.consumerfinance.ogwallet.data

actual object DataFileReader {
    actual fun readFile(path: String): String {
        // For WASM JS, file reading is not supported
        // For now, throw exception to fall back to sample data
        throw UnsupportedOperationException("File reading not supported on WASM JS")
    }
}