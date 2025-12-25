package dev.consumerfinance.ogwallet.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import kotlin.coroutines.resume

actual class BiometricAuth {
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
}