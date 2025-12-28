package dev.consumerfinance.ogwallet.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.Foundation.NSUserDefaults
import kotlin.coroutines.resume

actual class BiometricAuth {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val KEY_PIN_HASH = "pin_hash"
    private val KEY_ENCRYPTED_PASSPHRASE = "encrypted_passphrase"
    private val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    actual fun isBiometricAvailable(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun authenticate(): Result<String> = suspendCancellableCoroutine { continuation ->
        val context = LAContext()

        // Check if biometric authentication is available
        val canEvaluate = context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, null)

        if (canEvaluate) {
            // This triggers the native iOS FaceID/Passcode popup
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthentication,
                "Unlock your vault"
            ) { success, authError ->
                if (success) {
                    continuation.resume(Result.success(retrieveKeyFromKeychain()))
                } else {
                    val errorMsg = authError?.localizedDescription ?: "Authentication failed"
                    continuation.resume(Result.failure(Exception("Auth Failed: $errorMsg")))
                }
            }
        } else {
            continuation.resume(Result.failure(Exception("No Screen Lock set")))
        }
    }

    private fun retrieveKeyFromKeychain(): String {
        // TODO: Implement proper iOS Keychain integration
        // For now, return a placeholder passphrase
        return "placeholder_passphrase"
    }

    // --- PIN management methods ---
    actual fun setupMasterPIN(pin: String): Result<String> {
        return try {
            // For iOS, we'll store a simple hash and a generated passphrase
            val pinHash = pin.hashCode().toString()
            val passphrase = "ios_passphrase_${pinHash}"

            userDefaults.setObject(pinHash, KEY_PIN_HASH)
            userDefaults.setObject(passphrase, KEY_ENCRYPTED_PASSPHRASE)
            userDefaults.synchronize()

            Result.success(passphrase)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to setup PIN: ${e.message}", e))
        }
    }

    actual fun verifyMasterPIN(pin: String): Result<String> {
        return try {
            val storedHash = userDefaults.stringForKey(KEY_PIN_HASH)
            val inputHash = pin.hashCode().toString()

            if (storedHash == inputHash) {
                val passphrase = userDefaults.stringForKey(KEY_ENCRYPTED_PASSPHRASE) ?: "default_passphrase"
                Result.success(passphrase)
            } else {
                Result.failure(Exception("Incorrect PIN"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("PIN verification failed: ${e.message}", e))
        }
    }

    actual fun isPINSet(): Boolean {
        return userDefaults.objectForKey(KEY_PIN_HASH) != null
    }

    // --- Onboarding completion methods ---
    actual fun setOnboardingComplete(complete: Boolean) {
        userDefaults.setBool(complete, KEY_ONBOARDING_COMPLETE)
        userDefaults.synchronize()
    }

    actual fun isOnboardingComplete(): Boolean {
        return userDefaults.boolForKey(KEY_ONBOARDING_COMPLETE)
    }
}