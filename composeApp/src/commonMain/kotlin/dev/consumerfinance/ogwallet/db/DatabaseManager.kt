package dev.consumerfinance.ogwallet.db

import dev.ogwallet.db.OGVault
import dev.ogwallet.db.OGVaultQueries
import dev.ogwallet.db.Audit_log
import dev.ogwallet.db.Spending_alert
import dev.ogwallet.db.Vault_config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DatabaseManager(private val driverFactory: DriverFactory) {
    private var database: OGVault? = null

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    fun unlock(passphrase: String) {
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
            _isUnlocked.value = true
        } catch (e: Exception) {
            _isUnlocked.value = false
            // Logic for "Wrong PIN" or "Encryption Error" goes here
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
    fun completeOnboarding(userName: String = "User", currencyCode: String = "INR") {
        queries?.insertVaultConfig(
            user_name = userName,
            currency_code = currencyCode,
            is_biometric_enabled = true,
            theme_mode = dev.consumerfinance.ogwallet.models.ThemeMode.DARK
        )
    }
}