package dev.consumerfinance.ogwallet.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.getPlatform
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import dev.consumerfinance.ogwallet.models.ThemeMode
import dev.consumerfinance.ogwallet.auth.BiometricAuth // New import
import dev.consumerfinance.ogwallet.services.DataExportService
import org.koin.compose.koinInject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.io.OutputStreamWriter
import kotlinx.coroutines.launch // New import
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.text.input.VisualTransformation // New import
import androidx.compose.ui.text.input.PasswordVisualTransformation // New import
import androidx.compose.foundation.text.KeyboardOptions // New import
import androidx.compose.ui.text.input.KeyboardType // New import

import androidx.activity.compose.BackHandler // Import BackHandler
import dev.consumerfinance.ogwallet.db.DatabaseManager // Moved import
import dev.consumerfinance.ogwallet.ui.screens.settings.SettingsScreenRoute

@Composable
fun SettingsScreen() {
    var currentRoute by remember { mutableStateOf(SettingsScreenRoute.MAIN_SETTINGS) }

    // State for dialogs (these are not part of navigation stack)
    var showExportDataDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAutoLockTimeoutDialog by remember { mutableStateOf(false) }

    var exportedDataContent by remember { mutableStateOf<String?>(null) } // To hold data before saving

    val biometricAuth = koinInject<BiometricAuth>() // Inject BiometricAuth

    val dataExportService = koinInject<DataExportService>()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Get context for file operations

    val dbManager = koinInject<DatabaseManager>() // Inject DatabaseManager
    val currentCurrency by dbManager.getCurrencyCode().collectAsState(initial = "INR")
    val currentTheme by dbManager.getThemeMode().collectAsState(initial = ThemeMode.SYSTEM)
    val currentAutoLockTimeout by dbManager.getAutoLockTimeout().collectAsState(initial = 60L)
    var isBiometricEnabled by remember { mutableStateOf(biometricAuth.isBiometricEnabled()) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json") // MIME type for JSON
    ) { uri ->
        uri?.let { fileUri ->
            exportedDataContent?.let { content ->
                scope.launch {
                    try {
                        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(content)
                            }
                        }
                        // Optionally show a success message
                        println("Export successful to $fileUri")
                    } catch (e: Exception) {
                        // Optionally show an error message
                        println("Export failed: ${e.message}")
                        e.printStackTrace()
                    } finally {
                        exportedDataContent = null // Clear content after saving attempt
                    }
                }
            }
        }
    }

    // Platform detection for Android-specific features
    val platform = getPlatform()
    val isAndroid = platform.name.contains("Android")

    // Handle back press
    BackHandler(enabled = currentRoute != SettingsScreenRoute.MAIN_SETTINGS) {
        currentRoute = SettingsScreenRoute.MAIN_SETTINGS
    }

    when (currentRoute) {
        SettingsScreenRoute.SMS_SCANNER -> {
            SmsScannerScreenWrapper(onBack = { currentRoute = SettingsScreenRoute.MAIN_SETTINGS })
        }
        SettingsScreenRoute.MBOX_IMPORT -> {
            MboxImportScreen(onBack = { currentRoute = SettingsScreenRoute.MAIN_SETTINGS })
        }
        SettingsScreenRoute.MAIN_SETTINGS -> {
            SettingsScreenContent(
                isAndroid = isAndroid,
                scope = scope,
                biometricAuth = biometricAuth,
                isBiometricEnabled = isBiometricEnabled,
                setBiometricEnabled = { isBiometricEnabled = it },
                onOpenSmsScanner = { currentRoute = SettingsScreenRoute.SMS_SCANNER },
                onOpenMboxImport = { currentRoute = SettingsScreenRoute.MBOX_IMPORT },
                onExportDataClicked = { showExportDataDialog = true },
                onOpenThemeSelector = { showThemeDialog = true },
                onChangeMasterPinClicked = { showChangePinDialog = true },
                onOpenCurrencySelector = { showCurrencyDialog = true },
                onOpenAutoLockTimeoutSelector = { showAutoLockTimeoutDialog = true }
            )

            if (showExportDataDialog) {
                ExportDataDialog(
                    onDismiss = { showExportDataDialog = false },
                    onExport = { password ->
                        showExportDataDialog = false // Dismiss dialog immediately
                        scope.launch {
                            try {
                                val encryptedData = dataExportService.exportData(password)
                                exportedDataContent = encryptedData // Store encrypted data
                                // Launch the file picker to choose save location
                                createDocumentLauncher.launch("ogwallet_export.json") // Suggest default filename
                            } catch (e: Exception) {
                                // TODO: Handle encryption/export error, e.g., show a Snackbar
                                println("Error during data export: ${e.message}")
                            }
                        }
                    }
                )
            }

            if (showThemeDialog) {
                ThemeSelectionDialog(
                    currentTheme = currentTheme,
                    onDismiss = { showThemeDialog = false },
                    onThemeSelected = { themeMode ->
                        scope.launch {
                            dbManager.updateThemeMode(themeMode) // Save theme preference
                            // No need to apply immediately, App.kt will observe
                            println("Selected theme: $themeMode")
                        }
                        showThemeDialog = false
                    }
                )
            }

            if (showChangePinDialog) {
                ChangePinDialog(
                    onDismiss = { showChangePinDialog = false },
                    onChangePin = { oldPin, newPin ->
                        showChangePinDialog = false // Dismiss dialog immediately
                        scope.launch {
                            val oldPinVerificationResult = biometricAuth.verifyMasterPIN(oldPin)
                            if (oldPinVerificationResult.isSuccess) {
                                val newPinSetupResult = biometricAuth.setupMasterPIN(newPin)
                                if (newPinSetupResult.isSuccess) {
                                    println("PIN changed successfully")
                                    // TODO: Show success message to user
                                } else {
                                    println("Failed to set new PIN: ${newPinSetupResult.exceptionOrNull()?.message}")
                                    // TODO: Show error to user
                                }
                            } else {
                                println("Old PIN verification failed: ${oldPinVerificationResult.exceptionOrNull()?.message}")
                                // TODO: Show error to user
                            }
                        }
                    }
                )
            }

            if (showCurrencyDialog) {
                CurrencySelectionDialog(
                    currentCurrency = currentCurrency,
                    onDismiss = { showCurrencyDialog = false },
                    onCurrencySelected = { currencyCode ->
                        scope.launch {
                            dbManager.updateCurrencyCode(currencyCode) // Save currency preference
                            println("Selected currency: $currencyCode")
                        }
                        showCurrencyDialog = false
                    }
                )
            }

            if (showAutoLockTimeoutDialog) {
                AutoLockTimeoutDialog(
                    currentTimeout = currentAutoLockTimeout.toInt(),
                    onDismiss = { showAutoLockTimeoutDialog = false },
                    onTimeoutSelected = { timeoutSeconds ->
                        scope.launch {
                            dbManager.updateAutoLockTimeout(timeoutSeconds.toLong()) // Save timeout preference
                            println("Selected auto lock timeout: $timeoutSeconds seconds")
                        }
                        showAutoLockTimeoutDialog = false
                    }
                )
            }
        }
    }
}



@Composable
private fun SettingsScreenContent(
    isAndroid: Boolean,
    scope: CoroutineScope,
    biometricAuth: BiometricAuth,
    isBiometricEnabled: Boolean,
    setBiometricEnabled: (Boolean) -> Unit,
    onOpenSmsScanner: () -> Unit,
    onOpenMboxImport: () -> Unit,
    onExportDataClicked: () -> Unit,
    onOpenThemeSelector: () -> Unit,
    onChangeMasterPinClicked: () -> Unit, // New parameter
    onOpenCurrencySelector: () -> Unit, // Add this parameter
    onOpenAutoLockTimeoutSelector: () -> Unit // New parameter
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Manage your app preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Import & Data Section
        item {
            SettingsSection(title = "Import & Data") {
                SettingsItem(
                    icon = Icons.Default.Message,
                    title = "Import from SMS",
                    subtitle = "Scan SMS messages for transactions",
                    onClick = onOpenSmsScanner
                )

                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Import from Mbox",
                    subtitle = "Import transactions from email files",
                    onClick = onOpenMboxImport
                )

                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = "Export Data",
                    subtitle = "Export your data to a file",
                    onClick = onExportDataClicked
                )
            }
        }
        
        // Security Section
        item {
            SettingsSection(title = "Security & Privacy") {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Master PIN",
                    subtitle = "Update your security PIN",
                    onClick = onChangeMasterPinClicked // Pass the new lambda
                )
                
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Authentication",
                    subtitle = if (isBiometricEnabled) "Enabled - tap to disable" else "Disabled - tap to enable",
                    onClick = {
                        if (isAndroid) {
                            if (!isBiometricEnabled) {
                                // Try to enable biometric - prompt for authentication
                                scope.launch {
                                    val result = biometricAuth.authenticate()
                                    if (result.isSuccess) {
                                        biometricAuth.setBiometricEnabled(true)
                                        // Update state
                                        setBiometricEnabled(true)
                                    } else {
                                        // Show error or do nothing
                                        println("Biometric setup failed: ${result.exceptionOrNull()?.message}")
                                    }
                                }
                            } else {
                                // Disable biometric
                                biometricAuth.setBiometricEnabled(false)
                                setBiometricEnabled(false)
                            }
                        }
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Auto-lock Timeout",
                    subtitle = "Lock app after inactivity",
                    onClick = onOpenAutoLockTimeoutSelector
                )
            }
        }
        
        // Preferences Section
        item {
            SettingsSection(title = "Preferences") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "Light, Dark, or System",
                    onClick = onOpenThemeSelector // Pass the new lambda
                )
                
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification preferences",
                    onClick = {
                        // TODO: Open notification settings - Android specific
                        if (isAndroid) {
                            // Android-specific code would go here
                        }
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Currency",
                    subtitle = "Default currency for transactions",
                    onClick = onOpenCurrencySelector
                )
            }
        }

        // About Section
        item {
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0",
                    onClick = { /* TODO */ }
                )

                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = { /* TODO */ }
                )

                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Get help with OGWallet",
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun ExportDataDialog(
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Data") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Enter a password to encrypt your exported data.")

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Encryption Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showPassword,
                        onCheckedChange = { showPassword = it }
                    )
                    Text("Show Password", style = MaterialTheme.typography.bodySmall)
                }

                errorMessage?.let {
                    Text(
                        text = it,
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
                        password.isEmpty() -> errorMessage = "Password cannot be empty"
                        password != confirmPassword -> errorMessage = "Passwords do not match"
                        else -> onExport(password)
                    }
                }
            ) {
                Text("Export")
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
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val themeOptions = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
    var selectedOption by remember { mutableStateOf(currentTheme) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeOptions.forEach { themeMode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = themeMode },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == themeMode),
                            onClick = { selectedOption = themeMode }
                        )
                        Text(
                            text = themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onThemeSelected(selectedOption) }) {
                Text("Confirm")
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
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onChangePin: (oldPin: String, newPin: String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPin by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Master PIN") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Enter your current PIN and a new PIN.")

                OutlinedTextField(
                    value = oldPin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            oldPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Current PIN (6 digits)") },
                    singleLine = true,
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            newPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("New PIN (6 digits)") },
                    singleLine = true,
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmNewPin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            confirmNewPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Confirm New PIN") },
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

                errorMessage?.let {
                    Text(
                        text = it,
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
                        oldPin.isEmpty() || newPin.isEmpty() || confirmNewPin.isEmpty() -> errorMessage = "All PIN fields must be filled"
                        oldPin.length != 6 -> errorMessage = "Current PIN must be 6 digits"
                        newPin.length != 6 -> errorMessage = "New PIN must be 6 digits"
                        newPin != confirmNewPin -> errorMessage = "New PINs do not match"
                        else -> onChangePin(oldPin, newPin)
                    }
                }
            ) {
                Text("Change PIN")
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
fun CurrencySelectionDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    val currencyOptions = listOf("INR" to "Indian Rupee", "USD" to "US Dollar")
    var selectedOption by remember { mutableStateOf(currentCurrency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currencyOptions.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = code },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == code),
                            onClick = { selectedOption = code }
                        )
                        Text(
                            text = "$code ($name)",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCurrencySelected(selectedOption) }) {
                Text("Confirm")
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
fun AutoLockTimeoutDialog(
    currentTimeout: Int,
    onDismiss: () -> Unit,
    onTimeoutSelected: (Int) -> Unit
) {
    val timeoutOptions = listOf(
        30 to "30 seconds",
        60 to "1 minute",
        300 to "5 minutes",
        600 to "10 minutes",
        1800 to "30 minutes",
        0 to "Never"
    )
    var selectedOption by remember { mutableStateOf(currentTimeout) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Auto-lock Timeout") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeoutOptions.forEach { (seconds, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = seconds },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == seconds),
                            onClick = { selectedOption = seconds }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onTimeoutSelected(selectedOption) }) {
                Text("Confirm")
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
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

