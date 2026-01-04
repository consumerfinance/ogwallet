package dev.consumerfinance.ogwallet.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.auth.BiometricAuth
import dev.consumerfinance.ogwallet.getPlatform
import kotlinx.coroutines.CoroutineScope

enum class AuthState {
    INITIAL,
    PIN_SETUP_REQUIRED,
    PIN_ENTRY_REQUIRED,
    AUTHENTICATING
}

@Composable
fun LockScreen(dbManager: DatabaseManager) {
    val biometricAuth = koinInject<BiometricAuth>()
    val scope = rememberCoroutineScope()
    var authState by remember { mutableStateOf(AuthState.INITIAL) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val platform = getPlatform()
    val isDesktop = platform.name.contains("Java")
    val isWeb = platform.name.contains("Web") || platform.name.contains("Wasm")

    // Automatically trigger authentication when the screen appears
    LaunchedEffect(Unit) {
        if (isDesktop || isWeb) {
            // For desktop and web, check if we need PIN setup or entry
            authenticateDesktop(biometricAuth, dbManager, scope,
                onPINSetupRequired = { authState = AuthState.PIN_SETUP_REQUIRED },
                onPINEntryRequired = { authState = AuthState.PIN_ENTRY_REQUIRED },
                onError = { errorMessage = it }
            )
        } else {
            // For mobile, use biometric auth if enabled
            if (biometricAuth.isBiometricEnabled()) {
                authenticate(biometricAuth, dbManager, scope) { error ->
                    errorMessage = error
                }
            } else {
                // If biometric not enabled, go directly to PIN entry
                authState = AuthState.PIN_ENTRY_REQUIRED
            }
        }
    }

    // Show appropriate screen based on auth state
    when (authState) {
        AuthState.PIN_SETUP_REQUIRED -> {
            PINSetupScreen(
                onPINSet = { pin ->
                    println("PIN Setup: User entered PIN, attempting to setup...")
                    scope.launch {
                        setupPINAndUnlock(biometricAuth, dbManager, pin,
                            onSuccess = {
                                println("PIN Setup: Success!")
                            },
                            onError = { error ->
                                println("PIN Setup: Error - $error")
                                errorMessage = error
                            }
                        )
                    }
                }
            )
        }
        AuthState.PIN_ENTRY_REQUIRED -> {
            PINEntryScreen(
                onPINEntered = { pin ->
                    scope.launch {
                        verifyPINAndUnlock(biometricAuth, dbManager, pin,
                            onSuccess = { /* Will unlock automatically */ },
                            onError = { errorMessage = it }
                        )
                    }
                },
                errorMessage = errorMessage
            )
        }
        else -> {
            DefaultLockScreen(
                biometricAuth = biometricAuth,
                dbManager = dbManager,
                errorMessage = errorMessage,
                onErrorChange = { errorMessage = it },
                onSwitchToPINEntry = { authState = AuthState.PIN_ENTRY_REQUIRED }
            )
        }
    }
}

@Composable
fun DefaultLockScreen(
    biometricAuth: BiometricAuth,
    dbManager: DatabaseManager,
    errorMessage: String?,
    onErrorChange: (String?) -> Unit,
    onSwitchToPINEntry: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Vault Icon & Branding
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Vault Locked",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "OGWallet uses bank-grade encryption to protect your financial data.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))

        // Unlock Button
        Button(
            onClick = {
                authenticate(biometricAuth, dbManager, scope) { error ->
                    onErrorChange(error)
                    // If biometric fails and PIN is set, switch to PIN entry
                    if (biometricAuth.isPINSet()) {
                        onSwitchToPINEntry()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Fingerprint, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Unlock with Biometrics", fontSize = 18.sp)
        }

        // PIN Entry Button (if PIN is set)
        if (biometricAuth.isPINSet()) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { onSwitchToPINEntry() },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Use PIN Instead", fontSize = 16.sp)
            }
        }

        // Error Feedback
        errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Helper function to handle the authentication logic flow
 */
private fun authenticate(
    auth: BiometricAuth,
    db: DatabaseManager,
    scope: CoroutineScope,
    onError: (String) -> Unit
) {
    scope.launch {
        auth.authenticate().fold(
            onSuccess = { key ->
                db.unlock(key) // This triggers the state change in App.kt
            },
            onFailure = {
                onError("Authentication failed. Please try again.")
            }
        )
    }
}

/**
 * Desktop-specific authentication handler
 */
private fun authenticateDesktop(
    auth: BiometricAuth,
    db: DatabaseManager,
    scope: CoroutineScope,
    onPINSetupRequired: () -> Unit,
    onPINEntryRequired: () -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        auth.authenticate().fold(
            onSuccess = { key ->
                db.unlock(key)
            },
            onFailure = { exception ->
                when (exception.message) {
                    "PIN_NOT_SET" -> onPINSetupRequired()
                    "PIN_REQUIRED" -> onPINEntryRequired()
                    else -> onError(exception.message ?: "Authentication failed")
                }
            }
        )
    }
}

/**
 * Setup PIN and unlock vault
 */
private suspend fun setupPINAndUnlock(
    auth: BiometricAuth,
    db: DatabaseManager,
    pin: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val result = auth.setupMasterPIN(pin)

        result.fold(
            onSuccess = { key ->
                db.unlock(key)
                onSuccess()
            },
            onFailure = { exception ->
                onError(exception.message ?: "Failed to setup PIN")
            }
        )
    } catch (e: Exception) {
        onError("Failed to setup PIN: ${e.message}")
    }
}

/**
 * Verify PIN and unlock vault
 */
private suspend fun verifyPINAndUnlock(
    auth: BiometricAuth,
    db: DatabaseManager,
    pin: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val result = auth.verifyMasterPIN(pin)

        result.fold(
            onSuccess = { key ->
                db.unlock(key)
                onSuccess()
            },
            onFailure = { exception ->
                onError(exception.message ?: "Incorrect PIN")
            }
        )
    } catch (e: Exception) {
        onError("Failed to verify PIN: ${e.message}")
    }
}