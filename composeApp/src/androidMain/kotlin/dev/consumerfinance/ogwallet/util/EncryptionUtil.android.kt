package dev.consumerfinance.ogwallet.util

import dev.consumerfinance.ogwallet.util.AppContext
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class EncryptionUtil actual constructor(private val context: AppContext) : KoinComponent {

    private val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    private val ALGORITHM = "AES"
    private val KEY_SIZE = 256 // AES-256

    private val PBKDF2_ITERATIONS = 10000
    private val PBKDF2_KEY_SIZE = 256
    private val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"


    private fun generateSecretKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_SIZE / 8) // Key size in bits, PBEKeySpec expects in bits, SecretKeySpec expects in bytes
        return SecretKeySpec(factory.generateSecret(spec).encoded, ALGORITHM)
    }

    actual fun encrypt(data: String, password: String): String {
        val salt = ByteArray(16) // 16-byte salt
        SecureRandom().nextBytes(salt)

        val secretKey = generateSecretKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv

        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        // Combine salt, IV, and encrypted data
        val output = ByteArray(salt.size + iv.size + encryptedBytes.size)
        System.arraycopy(salt, 0, output, 0, salt.size)
        System.arraycopy(iv, 0, output, salt.size, iv.size)
        System.arraycopy(encryptedBytes, 0, output, salt.size + iv.size, encryptedBytes.size)

        return Base64.encodeToString(output, Base64.DEFAULT)
    }

    actual fun decrypt(encryptedData: String, password: String): String {
        val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)

        val salt = ByteArray(16)
        val iv = ByteArray(16) // AES/CBC uses 16-byte IV
        val encryptedBytes = ByteArray(decodedData.size - salt.size - iv.size)

        System.arraycopy(decodedData, 0, salt, 0, salt.size)
        System.arraycopy(decodedData, salt.size, iv, 0, iv.size)
        System.arraycopy(decodedData, salt.size + iv.size, encryptedBytes, 0, encryptedBytes.size)

        val secretKey = generateSecretKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
