package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Screen for scanning SMS messages and importing transactions
 * This is a common interface - actual implementation is platform-specific
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsScannerScreen(
    onBack: () -> Unit,
    onStartScan: (daysBack: Int) -> Unit,
    scanProgress: SmsScanProgress?,
    isScanning: Boolean
) {
    var selectedDays by remember { mutableStateOf(90) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from SMS") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Text(
                            "Automatic Transaction Import",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Scan your SMS messages to automatically import credit card transactions. " +
                            "We'll look for transaction alerts from major banks and extract the details.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Time Range Selection
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Select Time Range",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TimeRangeOption(
                        label = "Last 30 days",
                        days = 30,
                        selected = selectedDays == 30,
                        onClick = { selectedDays = 30 }
                    )
                    TimeRangeOption(
                        label = "Last 90 days (Recommended)",
                        days = 90,
                        selected = selectedDays == 90,
                        onClick = { selectedDays = 90 }
                    )
                    TimeRangeOption(
                        label = "Last 180 days",
                        days = 180,
                        selected = selectedDays == 180,
                        onClick = { selectedDays = 180 }
                    )
                    TimeRangeOption(
                        label = "Last 365 days",
                        days = 365,
                        selected = selectedDays == 365,
                        onClick = { selectedDays = 365 }
                    )
                }
            }
            
            // Progress Card (shown when scanning)
            if (isScanning && scanProgress != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!scanProgress.isComplete) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                if (scanProgress.isComplete) "Scan Complete!" else "Scanning Messages...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (!scanProgress.isComplete) {
                            LinearProgressIndicator(
                                progress = if (scanProgress.totalMessages > 0) {
                                    scanProgress.scannedMessages.toFloat() / scanProgress.totalMessages
                                } else 0f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Scanned", "${scanProgress.scannedMessages}/${scanProgress.totalMessages}")
                            StatItem("Found", "${scanProgress.transactionsFound}")
                            StatItem("Saved", "${scanProgress.transactionsSaved}")
                        }

                        if (scanProgress.error != null) {
                            Text(
                                "Error: ${scanProgress.error}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Start Scan Button
            Button(
                onClick = { onStartScan(selectedDays) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isScanning
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanning) "Scanning..." else "Start Scan")
            }

            // Privacy Notice
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Your SMS data is processed locally on your device and stored in an encrypted vault. " +
                        "No data is sent to external servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeRangeOption(
    label: String,
    days: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Data class for SMS scan progress
 */
data class SmsScanProgress(
    val totalMessages: Int,
    val scannedMessages: Int,
    val transactionsFound: Int,
    val transactionsSaved: Int,
    val isComplete: Boolean = false,
    val error: String? = null
)


