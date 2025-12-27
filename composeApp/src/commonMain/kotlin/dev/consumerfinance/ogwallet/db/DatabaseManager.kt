package dev.consumerfinance.ogwallet.db

import dev.ogwallet.db.OGVault
import dev.ogwallet.db.OGVaultQueries
import dev.ogwallet.db.Audit_log
import dev.ogwallet.db.Spending_alert
import dev.ogwallet.db.Vault_config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import dev.consumerfinance.ogwallet.models.ThemeMode
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers

class DatabaseManager(private val driverFactory: DriverFactory) {
    private var database: OGVault? = null

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    fun unlock(passphrase: String, isRetry: Boolean = false) {
        try {
            val driver = driverFactory.createDriver(passphrase)
            database = OGVault(
                driver = driver,
                audit_logAdapter = Audit_log.Adapter(
                    event_typeAdapter = auditEventTypeAdapter
                ),
                spending_alertAdapter = Spending_alert.Adapter(
                    alert_typeAdapter = alertTypeAdapter,
                    priorityAdapter = alertPriorityAdapter
                ),
                vault_configAdapter = Vault_config.Adapter(
                    theme_modeAdapter = themeModeAdapter
                )
            )
            // Create tables if they don't exist
            OGVault.Schema.create(driver)
            _isUnlocked.value = true
        } catch (e: Exception) {
            // Log the error for debugging
            println("DatabaseManager: Error unlocking vault - ${e.message}")

            // Handle database corruption or invalid file errors
            if (!isRetry && (e.message?.contains("file is not a database") == true ||
                             e.message?.contains("database disk image is malformed") == true ||
                             e.message?.contains("SQLITE_CORRUPT") == true)) {
                println("DatabaseManager: Database file appears corrupted or invalid. Attempting to delete and retry.")
                driverFactory.deleteDatabase("vault.db")
                unlock(passphrase, isRetry = true) // Retry once
                return
            }

            _isUnlocked.value = false
            // Logic for "Wrong PIN" or "Encryption Error" goes here
            // For now, rethrow if it's not the specific error we're handling or if it's a retry attempt
            throw e
        }
    }

    val queries: OGVaultQueries? get() = database?.oGVaultQueries

    /**
     * Check if onboarding is complete by checking if vault_config exists
     */
    fun isOnboardingComplete(): Boolean {
        return try {
            queries?.getVaultConfig()?.executeAsOneOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Complete onboarding by inserting initial vault config
     */

    fun completeOnboarding(userName: String = "User", monthlyBudget: Double = 0.0, currencyCode: String = "INR") {
        queries?.insertVaultConfig(
            user_name = userName,
            currency_code = currencyCode,
            is_biometric_enabled = true,
            theme_mode = ThemeMode.DARK, // Use the enum directly
            auto_lock_timeout = 300,
            monthly_budget = monthlyBudget
        )
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        queries?.updateThemeMode(themeMode)
    }

    fun updateCurrencyCode(currencyCode: String) {
        queries?.updateCurrencyCode(currencyCode)
    }

    fun updateAutoLockTimeout(timeoutSeconds: Long) {
        queries?.updateAutoLockTimeout(timeoutSeconds)
    }

    fun getThemeMode(): Flow<ThemeMode> {
        return queries?.getVaultConfig()
            ?.asFlow()
            ?.mapToOneOrNull(Dispatchers.Default)
            ?.map { vaultConfig ->
                vaultConfig?.theme_mode ?: ThemeMode.SYSTEM
            } ?: kotlinx.coroutines.flow.flowOf(ThemeMode.SYSTEM)
    }

    fun getUserName(): Flow<String> {
        return queries?.getUserName()
            ?.asFlow()
            ?.mapToOneOrNull(Dispatchers.Default)
            ?.map { userName ->
                userName ?: "User" // Default to "User" if not found
            } ?: kotlinx.coroutines.flow.flowOf("User")
    }

    fun getCurrencyCode(): Flow<String> {
        return queries?.getVaultConfig()
            ?.asFlow()
            ?.mapToOneOrNull(Dispatchers.Default)
            ?.map { vaultConfig ->
                vaultConfig?.currency_code ?: "INR" // Default to INR if not found
            } ?: kotlinx.coroutines.flow.flowOf("INR")
    }

    fun getCurrencyCodeSync(): String {
        return try {
            queries?.getVaultConfig()?.executeAsOneOrNull()?.currency_code ?: "INR"
        } catch (e: Exception) {
            "INR"
        }
    }

    fun getAutoLockTimeout(): Flow<Long> {
        return queries?.getVaultConfig()
            ?.asFlow()
            ?.mapToOneOrNull(Dispatchers.Default)
            ?.map { vaultConfig ->
                vaultConfig?.auto_lock_timeout ?: 300L // Default to 300 seconds if not found
            } ?: kotlinx.coroutines.flow.flowOf(300L)
    }

    fun getMonthlyBudget(): Flow<Double> {
        return queries?.getMonthlyBudget()
            ?.asFlow()
            ?.mapToOneOrNull(Dispatchers.Default)
            ?.map { it?.monthly_budget ?: 0.0 } ?: kotlinx.coroutines.flow.flowOf(0.0)
    }

    fun lock() {
        database = null
        _isUnlocked.value = false
    }
}