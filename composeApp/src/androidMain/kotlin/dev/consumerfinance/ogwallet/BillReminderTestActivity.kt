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
import dev.consumerfinance.ogwallet.db.BillRepository
import dev.consumerfinance.ogwallet.notifications.BillReminderNotificationManager
import dev.consumerfinance.ogwallet.notifications.BillReminderScheduler
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BillReminderTestActivity : ComponentActivity() {
    private val billRepository: BillRepository by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                BillReminderTestScreen(
                    billRepository = billRepository,
                    context = this,
                    onFinish = { finish() }
                )
            }
        }
    }
}

@Composable
fun BillReminderTestScreen(
    billRepository: BillRepository,
    context: android.content.Context,
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready to test bill reminders") }
    var isLoading by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Bill Reminder Test",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                status,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                try {
                                    billRepository.createSampleBill()
                                    status = "✅ Sample bill created (due in 3 days)"
                                } catch (e: Exception) {
                                    status = "❌ Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Sample Bill")
                    }
                    
                    Button(
                        onClick = {
                            val notificationManager = BillReminderNotificationManager(context)
                            notificationManager.sendTestNotification()
                            status = "📱 Test notification sent!"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Test Notification")
                    }
                    
                    Button(
                        onClick = {
                            BillReminderScheduler.triggerImmediateCheck(context)
                            status = "🔔 Immediate reminder check triggered"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Trigger Reminder Check")
                    }
                    
                    Button(
                        onClick = {
                            BillReminderScheduler.scheduleDailyBillCheck(context)
                            status = "⏰ Daily reminder check scheduled"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Schedule Daily Checks")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

