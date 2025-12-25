package dev.consumerfinance.ogwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.models.TransactionEntry
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.android.ext.android.inject

class TestDataActivity : ComponentActivity() {
    private val repository: TransactionRepository by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                TestDataScreen(repository) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun TestDataScreen(repository: TransactionRepository, onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready to insert test data") }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Test Data Inserter",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            insertTestTransactions(repository) { progress ->
                                status = progress
                            }
                            status = "✅ Successfully inserted 26 test transactions!"
                        } catch (e: Exception) {
                            status = "❌ Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            ) {
                Text("Insert Test Transactions")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onFinish) {
                Text("Close")
            }
        }
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
suspend fun insertTestTransactions(repository: TransactionRepository, onProgress: (String) -> Unit) {
    val testTransactions = listOf(
        // Food
        TransactionEntry(id = 0, amount = 450.0, merchant = "SWIGGY", category = "FOOD", cardHandle = "1234", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 850.0, merchant = "ZOMATO", category = "FOOD", cardHandle = "1234", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 1200.0, merchant = "DOMINOS PIZZA", category = "FOOD", cardHandle = "5678", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 320.0, merchant = "STARBUCKS", category = "FOOD", cardHandle = "1234", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 680.0, merchant = "CAFE COFFEE DAY", category = "FOOD", cardHandle = "9012", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 750.0, merchant = "BIGBASKET", category = "FOOD", cardHandle = "1234", timestamp = Clock.System.now()),

        // Shopping
        TransactionEntry(id = 0, amount = 2499.0, merchant = "AMAZON", category = "SHOPPING", cardHandle = "5678", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 3599.0, merchant = "FLIPKART", category = "SHOPPING", cardHandle = "9012", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 1299.0, merchant = "MYNTRA", category = "SHOPPING", cardHandle = "1234", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 899.0, merchant = "NYKAA", category = "SHOPPING", cardHandle = "5678", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 5600.0, merchant = "CROMA", category = "SHOPPING", cardHandle = "3456", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 2100.0, merchant = "RELIANCE DIGITAL", category = "SHOPPING", cardHandle = "5678", timestamp = Clock.System.now()),

        // Bills
        TransactionEntry(id = 0, amount = 1850.0, merchant = "AIRTEL PAYMENTS", category = "BILLS", cardHandle = "1234", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 2100.0, merchant = "TATA POWER", category = "BILLS", cardHandle = "9012", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 599.0, merchant = "NETFLIX", category = "BILLS", cardHandle = "5678", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 399.0, merchant = "AMAZON PRIME", category = "BILLS", cardHandle = "1234", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 1450.0, merchant = "BSNL RECHARGE", category = "BILLS", cardHandle = "3456", timestamp = Clock.System.now()),

        // Travel
        TransactionEntry(id = 0, amount = 4500.0, merchant = "MAKEMYTRIP", category = "TRAVEL", cardHandle = "9012", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 3200.0, merchant = "UBER", category = "TRAVEL", cardHandle = "5678", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 250.0, merchant = "OLA CABS", category = "TRAVEL", cardHandle = "1234", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 8999.0, merchant = "GOIBIBO", category = "TRAVEL", cardHandle = "9012", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 2350.0, merchant = "INDIGO AIRLINES", category = "TRAVEL", cardHandle = "3456", timestamp = Clock.System.now()),

        // Entertainment
        TransactionEntry(id = 0, amount = 450.0, merchant = "BOOKMYSHOW", category = "ENTERTAINMENT", cardHandle = "5678", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 1200.0, merchant = "PVR CINEMAS", category = "ENTERTAINMENT", cardHandle = "1234", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 299.0, merchant = "SPOTIFY", category = "ENTERTAINMENT", cardHandle = "9012", timestamp = Clock.System.now()),
        TransactionEntry(id = 0, amount = 199.0, merchant = "YOUTUBE PREMIUM", category = "ENTERTAINMENT", cardHandle = "5678", timestamp = Clock.System.now())
    )
    
    testTransactions.forEachIndexed { index, transaction ->
        onProgress("Inserting transaction ${index + 1}/${testTransactions.size}: ${transaction.merchant}")
        repository.addTransaction(transaction)
        kotlinx.coroutines.delay(100) // Small delay to show progress
    }
}

