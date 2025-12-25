package dev.consumerfinance.ogwallet.ui.screens

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

@Composable
fun SettingsScreen() {
    var showSmsScanner by remember { mutableStateOf(false) }
    var showMboxImport by remember { mutableStateOf(false) }
    
    // Check if we're on Android
    val platform = getPlatform()
    val isAndroid = platform.name.contains("Android")
    
    when {
        showSmsScanner && isAndroid -> {
            SmsScannerScreenWrapper(onBack = { showSmsScanner = false })
        }
        showMboxImport -> {
            ImportScreen()
        }
        else -> {
            SettingsScreenContent(
                isAndroid = isAndroid,
                onOpenSmsScanner = { showSmsScanner = true },
                onOpenMboxImport = { showMboxImport = true }
            )
        }
    }
}

@Composable
private fun SettingsScreenContent(
    isAndroid: Boolean,
    onOpenSmsScanner: () -> Unit,
    onOpenMboxImport: () -> Unit
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
                if (isAndroid) {
                    SettingsItem(
                        icon = Icons.Default.Message,
                        title = "Import from SMS",
                        subtitle = "Scan SMS messages for transactions",
                        onClick = onOpenSmsScanner
                    )
                }
                
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
                    onClick = { /* TODO */ }
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
                    onClick = { /* TODO */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Authentication",
                    subtitle = "Use fingerprint or face unlock",
                    onClick = { /* TODO */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Auto-lock Timeout",
                    subtitle = "Lock app after inactivity",
                    onClick = { /* TODO */ }
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
                    onClick = { /* TODO */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification preferences",
                    onClick = { /* TODO */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Currency",
                    subtitle = "Default currency for transactions",
                    onClick = { /* TODO */ }
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

