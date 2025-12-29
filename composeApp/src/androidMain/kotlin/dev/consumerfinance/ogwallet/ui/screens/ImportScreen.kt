package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Message
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.services.MboxImportService
import dev.consumerfinance.ogwallet.util.MboxImportResult
import dev.consumerfinance.ogwallet.utils.formatCurrency
import dev.consumerfinance.ogwallet.getPlatform
import dev.consumerfinance.ogwallet.ui.screens.settings.SmsScannerScreenWrapper
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ImportScreen() {
    var showSmsScanner by remember { mutableStateOf(false) }

    // Check if we're on Android
    val platform = getPlatform()
    val isAndroid = platform.name.contains("Android")

    if (showSmsScanner && isAndroid) {
        // Show SMS scanner screen (Android-specific)
        SmsScannerScreenWrapper(onBack = { showSmsScanner = false })
    } else {
        ImportScreenContent(
            isAndroid = isAndroid,
            onOpenSmsScanner = { showSmsScanner = true }
        )
    }
}

@Composable
private fun ImportScreenContent(
    isAndroid: Boolean,
    onOpenSmsScanner: () -> Unit
) {
    val repository = koinInject<TransactionRepository>()
    val dbManager = koinInject<DatabaseManager>()
    val importService = remember { MboxImportService(repository) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currencyCode by dbManager.getCurrencyCode().collectAsState(initial = "INR")

    var isImporting by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<MboxImportResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val pickMboxFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { fileUri ->
            scope.launch {
                isImporting = true
                errorMessage = null
                try {
                    val mboxContent = context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).readText()
                    }
                    val result = importService.importFromMbox(mboxContent)
                    importResult = result
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Import failed"
                } finally {
                    isImporting = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Import Transactions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Import your email transactions from mbox files",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // SMS Import Button (Android only)
        if (isAndroid) {
            Button(
                onClick = onOpenSmsScanner,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Message, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import from SMS")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Import Button
        Button(
            onClick = {
                pickMboxFileLauncher.launch("application/mbox")
            },
            enabled = !isImporting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importing...")
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select mbox File")
            }
        }
        
        // Results
        importResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.importedTransactions > 0)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (result.importedTransactions > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (result.importedTransactions > 0)
                                "Import Successful!"
                            else
                                "Import Completed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider()

                    Text("Total Messages: ${result.totalMessages}")
                    Text("Transactions Imported: ${result.importedTransactions}")
                    if (result.failedTransactions > 0) {
                        Text(
                            "Failed: ${result.failedTransactions}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text("Total Amount: ${formatCurrency(result.totalAmount, currencyCode)}")
                }
            }
        }
        
        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
