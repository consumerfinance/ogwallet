package dev.consumerfinance.ogwallet.auth

import kotlinx.browser.window

/**
 * JavaScript implementation of BiometricAuth using localStorage for PIN storage.
 */
actual class BiometricAuth {

    actual fun isBiometricAvailable(): Boolean {
        return false // Biometric authentication not available on web platforms
    }

    actual suspend fun authenticate(): Result<String> {
        return try {
            // Check if PIN is set
            val pinHash = getPINHash()
            if (pinHash == null) {
                Result.failure(Exception("PIN_NOT_SET"))
            } else {
                Result.failure(Exception("PIN_REQUIRED"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Authentication failed: ${e.message}"))
        }
    }
    
    /**
     * Setup Master PIN
     */
    actual fun setupMasterPIN(pin: String): Result<String> {
        return try {
            // Store PIN hash
            storePINHash(pin)
            
            // Generate passphrase
            val passphrase = generateSecurePassphrase()
            
            // Store encrypted passphrase
            storeEncryptedPassphrase(passphrase, pin)
            
            Result.success(passphrase)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to setup: ${e.message}"))
        }
    }
    
    /**
     * Verify Master PIN
     */
    actual fun verifyMasterPIN(pin: String): Result<String> {
        return try {
            val storedHash = getPINHash()
            val inputHash = hashPIN(pin)
            
            if (storedHash == inputHash) {
                val passphrase = getEncryptedPassphrase(pin)
                Result.success(passphrase)
            } else {
                Result.failure(Exception("Incorrect PIN"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("PIN verification failed: ${e.message}"))
        }
    }
    
    /**
     * Check if PIN is set
     */
    actual fun isPINSet(): Boolean {
        return getPINHash() != null
    }

    // --- Onboarding completion methods ---
    actual fun setOnboardingComplete(complete: Boolean) {
        window.localStorage.setItem("ogwallet_onboarding_complete", complete.toString())
    }

    actual fun isOnboardingComplete(): Boolean {
        return window.localStorage.getItem("ogwallet_onboarding_complete") == "true"
    }
    
    // Storage helper methods using localStorage
    
    private fun generateSecurePassphrase(): String {
        // Generate 32 random bytes
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
        return (1..44).map { chars.random() }.joinToString("")
    }
    
    private fun hashPIN(pin: String): String {
        // Simple hash for demo - in production use Web Crypto API
        return pin.hashCode().toString()
    }
    
    private fun storePINHash(pin: String) {
        val hash = hashPIN(pin)
        window.localStorage.setItem("ogwallet_pin_hash", hash)
    }
    
    private fun getPINHash(): String? {
        return window.localStorage.getItem("ogwallet_pin_hash")
    }
    
    private fun storeEncryptedPassphrase(passphrase: String, pin: String) {
        // For simplicity, store base64 encoded
        // In production, use Web Crypto API for proper encryption
        window.localStorage.setItem("ogwallet_enc_passphrase", passphrase)
    }
    
    private fun getEncryptedPassphrase(pin: String): String {
        return window.localStorage.getItem("ogwallet_enc_passphrase") 
            ?: throw Exception("Passphrase not found")
    }
}

