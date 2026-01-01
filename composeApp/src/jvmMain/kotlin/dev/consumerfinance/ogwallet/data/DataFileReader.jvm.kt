package dev.consumerfinance.ogwallet.data

import java.io.File

actual object DataFileReader {
    actual fun readFile(path: String): String {
        return File(path).readText()
    }
}