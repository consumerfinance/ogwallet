package dev.consumerfinance.ogwallet.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import dev.consumerfinance.ogwallet.OGWalletApplication
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.util.UUID

actual class BiometricAuth(private val activity: FragmentActivity) {
    actual fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    actual suspend fun authenticate(): Result<String> {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock OGWallet")
            .setSubtitle("Use your screen lock to access the vault")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        // Implementation involves showing BiometricPrompt and returning
        // a key retrieved from the Android Keystore
        return Result.success(retrieveKeyFromKeystore())
    }

    private fun retrieveKeyFromKeystore(): String {
        // TODO: Implement proper Android Keystore integration for biometric/device credential
        // For now, return a placeholder passphrase
        return "placeholder_passphrase"
    }

    // --- PIN methods implementation ---
    private val PREFS_FILE_NAME = "ogwallet_pin_prefs"
    private val KEY_PIN_HASH = "pin_hash"
    private val KEY_ENCRYPTED_PASSPHRASE = "encrypted_passphrase"
    private val KEY_IV = "iv"
    private val KEY_SALT = "salt"

    private fun getSharedPreferences(): SharedPreferences {
        return OGWalletApplication.appContext.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    private fun hashPIN(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun deriveKeyFromPIN(pin: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val secretKey = factory.generateSecret(spec).encoded
        return SecretKeySpec(secretKey, "AES")
    }

    private fun encryptPassphrase(passphrase: String, key: SecretKeySpec, salt: ByteArray): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(salt.copyOfRange(0, 16))) // Use part of salt as IV
        val encryptedBytes = cipher.doFinal(passphrase.toByteArray(Charsets.UTF_8))
        return Pair(Base64.encodeToString(encryptedBytes, Base64.NO_WRAP), Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
    }

    private fun decryptPassphrase(encryptedPassphrase: String, key: SecretKeySpec, iv: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(Base64.decode(iv, Base64.NO_WRAP)))
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedPassphrase, Base64.NO_WRAP))
        return String(decryptedBytes, Charsets.UTF_8)
    }

    actual fun setupMasterPIN(pin: String): Result<String> {
        return try {
            val prefs = getSharedPreferences()
            val editor = prefs.edit()

            val salt = generateSalt()
            val hashedPIN = hashPIN(pin, salt)
            editor.putString(KEY_PIN_HASH, hashedPIN)
            editor.putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))

            // Generate a new passphrase
            val newPassphrase = UUID.randomUUID().toString()
            val key = deriveKeyFromPIN(pin, salt)
            val (encryptedPassphrase, iv) = encryptPassphrase(newPassphrase, key, salt)

            editor.putString(KEY_ENCRYPTED_PASSPHRASE, encryptedPassphrase)
            editor.putString(KEY_IV, iv)
            editor.apply()

            Result.success(newPassphrase)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to setup PIN: ${e.message}", e))
        }
    }

    actual fun verifyMasterPIN(pin: String): Result<String> {
        return try {
            val prefs = getSharedPreferences()
            val storedPinHash = prefs.getString(KEY_PIN_HASH, null)
            val storedSalt = prefs.getString(KEY_SALT, null)
            val storedEncryptedPassphrase = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
            val storedIv = prefs.getString(KEY_IV, null)

            if (storedPinHash == null || storedSalt == null || storedEncryptedPassphrase == null || storedIv == null) {
                return Result.failure(Exception("PIN not set or corrupted data"))
            }

            val saltBytes = Base64.decode(storedSalt, Base64.NO_WRAP)
            val inputHash = hashPIN(pin, saltBytes)

            if (inputHash != storedPinHash) {
                return Result.failure(Exception("Incorrect PIN"))
            }

            val key = deriveKeyFromPIN(pin, saltBytes)
            val decryptedPassphrase = decryptPassphrase(storedEncryptedPassphrase, key, storedIv)

            Result.success(decryptedPassphrase)
        } catch (e: Exception) {
            Result.failure(Exception("PIN verification failed: ${e.message}", e))
        }
    }

    actual fun isPINSet(): Boolean {
        val prefs = getSharedPreferences()
        return prefs.contains(KEY_PIN_HASH) && prefs.contains(KEY_ENCRYPTED_PASSPHRASE)
    }
}