package dev.consumerfinance.ogwallet.util

import dev.consumerfinance.ogwallet.util.AppContext

expect class EncryptionUtil(context: AppContext) {
    fun encrypt(data: String, password: String): String
    fun decrypt(encryptedData: String, password: String): String
}
