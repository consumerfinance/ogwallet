package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import org.koin.compose.koinInject
import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.utils.formatCurrency
import dev.consumerfinance.ogwallet.models.TransactionEntry
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
data class ConnectedAccount(
    val id: String,
    val name: String,
    val type: String,
    val balance: Double,
    val icon: ImageVector,
    val color: Color
)

data class WalletActivity(
    val id: String,
    val type: String,
    val description: String,
    val amount: Double,
    val date: String,
    val status: String
)



@Preview
@Composable
fun BudgetScreen() {
    val transactionRepository = koinInject<TransactionRepository>()
    val dbManager: DatabaseManager = koinInject()
    val allTransactions by transactionRepository.getAllTransactions().collectAsState(emptyList())
    val currencyCode by dbManager.getCurrencyCode().collectAsState(initial = "INR")

    val walletBalance = remember(allTransactions) {
        allTransactions.sumOf { it.amount }
    }

    val connectedAccounts = remember(allTransactions) {
        allTransactions
            .groupBy { it.cardHandle ?: "Unknown Card" } // Group by cardHandle
            .map { (cardHandle, transactions) ->
                val balance = transactions.sumOf { it.amount }
                ConnectedAccount(
                    id = cardHandle,
                    name = cardHandle, // Use cardHandle as name for now
                    type = "Card", // Default to "Card"
                    balance = balance,
                    icon = Icons.Filled.CreditCard, // Default icon
                    color = Color(0xFF8b5cf6) // Default color
                )
            }
    }

    val recentActivity = remember(allTransactions) {
        allTransactions.map { transaction ->
            WalletActivity(
                id = transaction.id.toString(),
                type = transaction.category, // Map category to type
                description = transaction.merchant,
                amount = transaction.amount,
                date = transaction.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
                status = "completed" // Assuming all fetched transactions are completed
            )
        }
    }



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
                    "Wallet",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Manage accounts and transfers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Wallet Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3b82f6), Color(0xFF8b5cf6))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "Total Balance",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    formatCurrency(walletBalance, currencyCode),
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))


                    }
                }
            }
        }

        // Connected Accounts
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Connected Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        connectedAccounts.forEachIndexed { index, account ->
                            AccountItem(account, currencyCode)
                            if (index < connectedAccounts.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        // Recent Activity
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { }) {
                        Text("See all")
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        recentActivity.forEachIndexed { index, activity ->
                            ActivityItem(activity, currencyCode)
                            if (index < recentActivity.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun AccountItem(account: ConnectedAccount, currencyCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = account.color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = account.icon,
                    contentDescription = null,
                    tint = account.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                account.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                account.type,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatCurrency(abs(account.balance), currencyCode),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (account.balance < 0) Color(0xFFef4444) else MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ActivityItem(activity: WalletActivity, currencyCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = if (activity.amount > 0) Color(0xFF10b981).copy(alpha = 0.1f) else Color(0xFF3b82f6).copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (activity.amount > 0) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = if (activity.amount > 0) Color(0xFF10b981) else Color(0xFF3b82f6),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                activity.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                activity.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            formatCurrency(abs(activity.amount), currencyCode),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (activity.amount < 0) Color(0xFFef4444) else if (activity.amount > 0) Color(0xFF10b981) else MaterialTheme.colorScheme.onSurface
        )
    }
}


