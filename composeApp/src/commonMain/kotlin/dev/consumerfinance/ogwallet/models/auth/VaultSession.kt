package dev.consumerfinance.ogwallet.models.auth

import kotlin.time.ExperimentalTime

// Represents the current security status of the vault
sealed class VaultSession {
    object Locked : VaultSession()
    data class Unlocked @OptIn(ExperimentalTime::class) constructor(val sessionToken: String, val lastActive: kotlinx.datetime.Instant) : VaultSession()
    data class Error(val message: String) : VaultSession()
}