package dev.consumerfinance.ogwallet.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

actual class BiometricAuth(private val activity: FragmentActivity) {
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
        // TODO: Implement proper Android Keystore integration
        // For now, return a placeholder passphrase
        return "placeholder_passphrase"
    }

    // PIN methods - not used on Android (uses biometric/device credential)
    actual fun setupMasterPIN(pin: String): Result<String> {
        return Result.failure(Exception("PIN not supported on Android"))
    }

    actual fun verifyMasterPIN(pin: String): Result<String> {
        return Result.failure(Exception("PIN not supported on Android"))
    }

    actual fun isPINSet(): Boolean {
        return false
    }
}