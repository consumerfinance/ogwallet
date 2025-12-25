package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.auth.BiometricAuth

// Data class to hold state during onboarding
data class InitialSetup(
    val cardName: String = "",
    val spendGoal: String = "",
    val deadlineDays: String = "90"
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var setup by remember { mutableStateOf(InitialSetup()) }
    var showPINSetup by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val dbManager = koinInject<DatabaseManager>()
    val biometricAuth = koinInject<BiometricAuth>()

    // Show PIN setup dialog if needed
    if (showPINSetup) {
        println("Onboarding: Rendering PIN setup dialog")
        PINSetupDialog(
            onPINSet = { pin ->
                println("Onboarding: PIN entered, setting up...")
                scope.launch {
                    val result = biometricAuth.setupMasterPIN(pin)
                    println("Onboarding: Setup result = ${result.isSuccess}")
                    result.fold(
                        onSuccess = { key ->
                            println("Onboarding: PIN setup success, unlocking vault")
                            dbManager.unlock(key)
                            dbManager.completeOnboarding()
                            showPINSetup = false
                            onFinished()
                        },
                        onFailure = { exception ->
                            println("Onboarding: PIN setup failed - ${exception.message}")
                            errorMessage = exception.message ?: "Failed to setup PIN"
                        }
                    )
                }
            },
            onDismiss = {
                println("Onboarding: PIN setup dismissed")
                showPINSetup = false
            },
            errorMessage = errorMessage
        )
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    Text(
                        text = "Step ${pagerState.currentPage + 1} of 3",
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (isProcessing) return@FloatingActionButton

                            if (pagerState.currentPage < 2) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                // FINAL STEP: Initialize the secure vault
                                isProcessing = true
                                println("Onboarding: Final step - attempting authentication")
                                scope.launch {
                                    val authResult = biometricAuth.authenticate()
                                    println("Onboarding: Auth result = ${authResult.isSuccess}")
                                    authResult.fold(
                                        onSuccess = { key ->
                                            println("Onboarding: Auth success, unlocking vault")
                                            dbManager.unlock(key)
                                            dbManager.completeOnboarding()
                                            onFinished()
                                        },
                                        onFailure = { exception ->
                                            println("Onboarding: Auth failed - ${exception.message}")
                                            // Check if we need PIN setup
                                            when (exception.message) {
                                                "PIN_NOT_SET" -> {
                                                    println("Onboarding: Showing PIN setup dialog")
                                                    showPINSetup = true
                                                }
                                                "PIN_REQUIRED" -> {
                                                    // During onboarding, if PIN is required, show PIN entry
                                                    println("Onboarding: PIN required, showing PIN entry")
                                                    showPINSetup = true
                                                }
                                                else -> {
                                                    println("Onboarding: Other error - ${exception.message}")
                                                    errorMessage = exception.message ?: "Authentication failed"
                                                    isProcessing = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        val icon = if (pagerState.currentPage == 2) Icons.Default.Check else Icons.Default.ArrowForward
                        Icon(icon, contentDescription = "Next")
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            userScrollEnabled = false // Control navigation via buttons
        ) { page ->
            when (page) {
                0 -> WelcomeStep()
                1 -> CardSetupStep(setup) { setup = it }
                2 -> SecurityStep()
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to OGWallet", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "We automatically track your credit card milestones by securely reading bank SMS notifications.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CardSetupStep(setup: InitialSetup, onUpdate: (InitialSetup) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Let's add your first card", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = setup.cardName,
            onValueChange = { onUpdate(setup.copy(cardName = it)) },
            label = { Text("Card Name (e.g., Amex Gold)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = setup.spendGoal,
            onValueChange = { onUpdate(setup.copy(spendGoal = it)) },
            label = { Text("Spending Goal ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SecurityStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bank-Grade Security", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            "Your data is encrypted with SQLCipher and locked behind your device's biometric hardware. " +
                    "No one, not even us, can see your transactions.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PINSetupDialog(
    onPINSet: (String) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Up Master PIN") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Create a 6-digit PIN to secure your vault",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            pin = it
                            localError = null
                        }
                    },
                    label = { Text("Enter PIN (6 digits)") },
                    visualTransformation = if (showPin) androidx.compose.ui.text.input.VisualTransformation.None
                                          else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            confirmPin = it
                            localError = null
                        }
                    },
                    label = { Text("Confirm PIN") },
                    visualTransformation = if (showPin) androidx.compose.ui.text.input.VisualTransformation.None
                                          else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = localError != null || errorMessage != null
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showPin,
                        onCheckedChange = { showPin = it }
                    )
                    Text("Show PIN", style = MaterialTheme.typography.bodySmall)
                }

                if (localError != null || errorMessage != null) {
                    Text(
                        text = localError ?: errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        pin.length != 6 -> {
                            localError = "PIN must be exactly 6 digits"
                        }
                        pin != confirmPin -> {
                            localError = "PINs do not match"
                        }
                        else -> {
                            onPINSet(pin)
                        }
                    }
                },
                enabled = pin.length == 6 && confirmPin.length == 6
            ) {
                Text("Set PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}