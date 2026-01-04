package dev.consumerfinance.ogwallet.auth

expect class BiometricAuth {
    suspend fun authenticate(): Result<String> // Returns the derived key for SQLCipher
    fun isBiometricAvailable(): Boolean
    fun isBiometricEnabled(): Boolean
    fun setBiometricEnabled(enabled: Boolean)

    // PIN management methods (for desktop platforms)
    fun setupMasterPIN(pin: String): Result<String>
    fun verifyMasterPIN(pin: String): Result<String>
    fun isPINSet(): Boolean

    // Onboarding completion tracking
    fun setOnboardingComplete(complete: Boolean)
    fun isOnboardingComplete(): Boolean
}