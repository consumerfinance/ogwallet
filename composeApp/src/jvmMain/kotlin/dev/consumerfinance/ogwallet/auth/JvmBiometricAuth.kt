package dev.consumerfinance.ogwallet.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

/**
 * Desktop implementation of BiometricAuth.
 * Uses Master PIN with PBKDF2 key derivation for secure vault encryption.
 *
 * For browser-based Compose Multiplatform apps, WebAuthn/Passkeys would be ideal,
 * but for desktop JVM apps, we use a secure PIN-based approach.
 */
actual class BiometricAuth {

    private val configDir = File(System.getProperty("user.home"), ".ogwallet")
    private val pinFile = File(configDir, ".pin")
    private val passphraseFile = File(configDir, ".passphrase")
    private val onboardingFile = File(configDir, ".onboarding_complete")

    actual suspend fun authenticate(): Result<String> = withContext(Dispatchers.IO) {
        try {
            authenticateWithMasterPIN()
        } catch (e: Exception) {
            Result.failure(Exception("Authentication failed: ${e.message}"))
        }
    }

    /**
     * Authenticate using Master PIN
     */
    private suspend fun authenticateWithMasterPIN(): Result<String> {
        // Check if PIN is set
        if (!pinFile.exists()) {
            // First time setup - need to create PIN
            return Result.failure(Exception("PIN_NOT_SET"))
        }

        // PIN verification will be handled by UI
        // For now, return a signal that PIN is required
        return Result.failure(Exception("PIN_REQUIRED"))
    }

    /**
     * Set up a new Master PIN
     */
    actual fun setupMasterPIN(pin: String): Result<String> {
        try {
            if (!configDir.exists()) {
                configDir.mkdirs()
            }

            // Generate a random passphrase for the vault
            val passphrase = generateSecurePassphrase()

            // Derive encryption key from PIN using PBKDF2
            val pinKey = derivePINToKey(pin)

            // Encrypt the passphrase with the PIN-derived key
            val encryptedPassphrase = encryptPassphrase(passphrase, pinKey)

            // Store hashed PIN for verification
            val hashedPIN = hashPIN(pin)
            pinFile.writeText(hashedPIN)

            // Store encrypted passphrase
            passphraseFile.writeText(encryptedPassphrase)

            println("PIN setup complete - passphrase encrypted and stored")
            return Result.success(passphrase)
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to setup PIN: ${e.message}"))
        }
    }

    /**
     * Verify Master PIN and return passphrase
     */
    actual fun verifyMasterPIN(pin: String): Result<String> {
        try {
            if (!pinFile.exists() || !passphraseFile.exists()) {
                return Result.failure(Exception("PIN not set"))
            }

            val storedHash = pinFile.readText()
            val inputHash = hashPIN(pin)

            if (storedHash == inputHash) {
                // Derive key from PIN
                val pinKey = derivePINToKey(pin)

                // Decrypt the passphrase
                val encryptedPassphrase = passphraseFile.readText()
                val passphrase = decryptPassphrase(encryptedPassphrase, pinKey)

                return Result.success(passphrase)
            } else {
                return Result.failure(Exception("Incorrect PIN"))
            }
        } catch (e: Exception) {
            return Result.failure(Exception("PIN verification failed: ${e.message}"))
        }
    }

    /**
     * Check if PIN is already set
     */
    actual fun isPINSet(): Boolean {
        return pinFile.exists()
    }

    /**
     * Generate a secure random passphrase for the vault
     */
    private fun generateSecurePassphrase(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * Derive encryption key from PIN using PBKDF2
     */
    private fun derivePINToKey(pin: String): ByteArray {
        val salt = "OGWallet_PIN_Salt_v1".toByteArray()
        val spec = PBEKeySpec(pin.toCharArray(), salt, 100000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    /**
     * Hash PIN for storage verification
     */
    private fun hashPIN(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }

    /**
     * Encrypt passphrase with PIN-derived key using AES
     */
    private fun encryptPassphrase(passphrase: String, key: ByteArray): String {
        val cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = javax.crypto.spec.SecretKeySpec(key, "AES")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(passphrase.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    /**
     * Decrypt passphrase with PIN-derived key using AES
     */
    private fun decryptPassphrase(encryptedPassphrase: String, key: ByteArray): String {
        val cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = javax.crypto.spec.SecretKeySpec(key, "AES")
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey)
        val encrypted = Base64.getDecoder().decode(encryptedPassphrase)
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted)
    }

    // --- Onboarding completion methods ---
    actual fun setOnboardingComplete(complete: Boolean) {
        try {
            if (!configDir.exists()) {
                configDir.mkdirs()
            }
            onboardingFile.writeText(complete.toString())
        } catch (e: Exception) {
            // Log error but don't throw
            println("Failed to set onboarding status: ${e.message}")
        }
    }

    actual fun isOnboardingComplete(): Boolean {
        return try {
            onboardingFile.exists() && onboardingFile.readText() == "true"
        } catch (e: Exception) {
            false
        }
    }
}

