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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.auth.BiometricAuth
import dev.consumerfinance.ogwallet.SmsReader
import dev.consumerfinance.ogwallet.util.SmsParser
import dev.consumerfinance.ogwallet.db.TransactionRepository
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import dev.consumerfinance.ogwallet.models.TransactionEntry
import kotlinx.datetime.Clock

// Data class to hold state during onboarding
data class InitialSetup(
    val userName: String = "",
    val monthlyBudget: Double = 0.0, // New field for monthly budget
    val cardName: String = "",
    val spendGoal: String = "",
    val deadlineDays: String = "90",
    val pin: String = "",
    val confirmPin: String = ""
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    var setup by remember { mutableStateOf(InitialSetup()) }
    var showPINSetup by remember { mutableStateOf(false) }
    var showPINVerification by remember { mutableStateOf(false) } // New state for PIN verification dialog
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }

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
                            dbManager.completeOnboarding(setup.userName)
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
                isProcessing = false
                errorMessage = null
            },
            errorMessage = errorMessage
        )
    }

    // Show PIN verification dialog if needed
    if (showPINVerification) {
        println("Onboarding: Rendering PIN verification dialog")
        PINVerificationDialog(
            onDismiss = {
                showPINVerification = false
                isProcessing = false
                errorMessage = null
            },
            onVerify = { pin ->
                scope.launch {
                    val result = biometricAuth.verifyMasterPIN(pin)
                    if (result.isSuccess) {
                        dbManager.unlock(result.getOrThrow()) // Unlock with the verified key
                        dbManager.completeOnboarding(setup.userName) // Pass username after unlock
                        showPINVerification = false
                        onFinished()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Incorrect PIN"
                    }
                }
            },
            errorMessage = errorMessage
        )
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    Text(
                        text = "Step ${pagerState.currentPage + 1} of 5",
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (isProcessing) return@FloatingActionButton

                            if (pagerState.currentPage == 3) {
                                // PIN setup step
                                when {
                                    setup.pin.length != 6 -> {
                                        errorMessage = "PIN must be exactly 6 digits"
                                    }
                                    setup.pin != setup.confirmPin -> {
                                        errorMessage = "PINs do not match"
                                    }
                                    else -> {
                                        isProcessing = true
                                        scope.launch {
                                            val result = biometricAuth.setupMasterPIN(setup.pin)
                                            println("Onboarding: Setup result = ${result.isSuccess}")
                                            result.fold(
                                                onSuccess = { key ->
                                                    println("Onboarding: PIN setup success")
                                                    errorMessage = null
                                                    scope.launch {
                                                        pagerState.animateScrollToPage(4)
                                                    }
                                                    isProcessing = false
                                                },
                                                onFailure = { exception ->
                                                    println("Onboarding: PIN setup failed - ${exception.message}")
                                                    errorMessage = exception.message ?: "Failed to setup PIN"
                                                    isProcessing = false
                                                }
                                            )
                                        }
                                    }
                                }
                            } else if (pagerState.currentPage < 4) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                // FINAL STEP: Initialize the secure vault
                                isProcessing = true
                                println("Onboarding: Final step - checking biometric availability")
                                if (biometricAuth.isBiometricAvailable()) {
                                    println("Onboarding: Biometric available, attempting authentication")
                                    scope.launch {
                                        val authResult = biometricAuth.authenticate()
                                        println("Onboarding: Auth result = ${authResult.isSuccess}")
                                        authResult.fold(
                                            onSuccess = { key ->
                                                // Save user name after successful authentication
                                                println("Onboarding: Auth success, unlocking vault")
                                                dbManager.unlock(key)
                                                dbManager.completeOnboarding(setup.userName)
                                                onFinished()
                                            },
                                            onFailure = { exception ->
                                                println("Onboarding: Auth failed - ${exception.message}")
                                                // Check if we need PIN setup or verification
                                                if (biometricAuth.isPINSet()) {
                                                    println("Onboarding: PIN is set, showing PIN verification dialog")
                                                    showPINVerification = true
                                                } else {
                                                    println("Onboarding: PIN not set, showing PIN setup dialog")
                                                    showPINSetup = true
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    println("Onboarding: Biometric not available, checking PIN")
                                    // Biometric not available, use PIN
                                    if (biometricAuth.isPINSet()) {
                                        println("Onboarding: PIN is set, showing PIN verification dialog")
                                        showPINVerification = true
                                    } else {
                                        println("Onboarding: PIN not set, showing PIN setup dialog")
                                        showPINSetup = true
                                    }
                                }
                            }
                        }
                    ) {
                        val icon = if (pagerState.currentPage == 4) Icons.Default.Check else Icons.Default.ArrowForward
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
                1 -> UserNameStep(
                    userName = setup.userName,
                    onUserNameChange = { setup = setup.copy(userName = it) }
                )
                2 -> SecurityStep()
                3 -> PINSetupStep(
                    pin = setup.pin,
                    onPinChange = { setup = setup.copy(pin = it) },
                    confirmPin = setup.confirmPin,
                    onConfirmPinChange = { setup = setup.copy(confirmPin = it) },
                    showPin = showPin,
                    onShowPinChange = { showPin = it },
                    errorMessage = errorMessage
                )
                4 -> SmsScanningStep()
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
fun UserNameStep(
    userName: String,
    onUserNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("What's your name?", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text("Your Name") },
            singleLine = true,
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
fun PINSetupStep(
    pin: String,
    onPinChange: (String) -> Unit,
    confirmPin: String,
    onConfirmPinChange: (String) -> Unit,
    showPin: Boolean,
    onShowPinChange: (Boolean) -> Unit,
    errorMessage: String?
) {
    val pinVisualTransformation = remember(showPin) {
        if (showPin) VisualTransformation.None else PasswordVisualTransformation()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Set Up Master PIN", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "Create a 6-digit PIN to secure your vault",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    onPinChange(it)
                }
            },
            label = { Text("Enter PIN (6 digits)") },
            visualTransformation = pinVisualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    onConfirmPinChange(it)
                }
            },
            label = { Text("Confirm PIN") },
            visualTransformation = pinVisualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showPin,
                onCheckedChange = onShowPinChange
            )
            Text("Show PIN", style = MaterialTheme.typography.bodySmall)
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SmsScanningStep() {
    val transactionRepository = koinInject<TransactionRepository>()
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var savedTransactionCount by remember { mutableStateOf<Int?>(null) }
    val smsReader = koinInject<SmsReader>()
    val smsParser = koinInject<SmsParser>() // Inject SmsParser

    // TODO: Need to handle READ_SMS permission request here.
    // This will likely involve a permission launcher similar to MainActivity.
    // For now, assume permission is granted or handle it in a more robust way later.

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scan SMS for Transactions", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            "To automatically populate your wallet, we need to scan your SMS messages for bank transactions.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        if (isScanning) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                scope.launch {
                    isScanning = true
                    val messages = smsReader.readSmsMessages()
                    val transactions = messages.mapNotNull { smsParser.parse(it) }
                    transactions.forEach { match ->
                        transactionRepository.addTransaction(
                            TransactionEntry(
                                id = 0, // Database will generate ID
                                amount = match.amount,
                                merchant = match.merchantRaw,
                                category = "OTHER", // Default category for now
                                timestamp = Clock.System.now(), // Current time
                                cardHandle = match.accountHandle
                            )
                        )
                    }
                    savedTransactionCount = transactions.size
                    isScanning = false
                }
            }) {
                Text("Scan SMS")
            }
        }
        savedTransactionCount?.let {
            Spacer(Modifier.height(16.dp))
            Text("Saved $it transactions")
        }
    }
}


@Composable
fun PINVerificationDialog(
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit,
    errorMessage: String? = null
) {
    var pin by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var showPin by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Master PIN") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Biometric authentication failed or is unavailable. Please enter your Master PIN.",
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
                    singleLine = true,
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
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
                        else -> {
                            onVerify(pin)
                        }
                    }
                },
                enabled = pin.length == 6
            ) {
                Text("Verify PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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

    val pinVisualTransformation = remember(showPin) {
        if (showPin) VisualTransformation.None else PasswordVisualTransformation()
    }

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
                    visualTransformation = pinVisualTransformation,
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
                    visualTransformation = pinVisualTransformation,
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