package dev.consumerfinance.ogwallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.models.CreditCard
import dev.consumerfinance.ogwallet.models.TransactionEntry
import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.db.DatabaseManager // New import
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime
import dev.consumerfinance.ogwallet.utils.formatCurrency


@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun DashboardScreen(onNavigate: (Int) -> Unit) {
    val repository: TransactionRepository = koinInject()
    val dbManager: DatabaseManager = koinInject() // Inject DatabaseManager
    val transactions by repository.getAllTransactions().collectAsState(initial = emptyList())
    val categoryBreakdown by repository.getSpendingBreakdown().collectAsState(initial = emptyList())
    val userName by dbManager.getUserName().collectAsState(initial = "User") // Collect userName
    val currencyCode by dbManager.getCurrencyCode().collectAsState(initial = "INR") // Collect currencyCode

    val cards = remember(transactions) {
        transactions
            .groupBy { it.cardHandle ?: "Unknown" }
            .map { (cardHandle, cardTransactions) ->
                val balance = cardTransactions.sumOf { it.amount }
                CreditCard(
                    id = cardHandle, // Use cardHandle as ID
                    name = "$cardHandle Card", // Placeholder name
                    last4 = cardHandle.takeLast(4), // Assume last 4 digits are in cardHandle
                    balance = balance,
                    limit = 0, // Placeholder
                    availableCredit = 0.0, // Placeholder
                    gradient = listOf(Color(0xFF334155), Color(0xFF0f172a)), // Default gradient
                    network = "", // Placeholder
                    expiry = "", // Placeholder
                    nextPayment = "", // Placeholder
                    minPayment = 0.0, // Placeholder
                    cvv = "",
                    cardNumber = cardHandle.takeLast(4) // Placeholder
                )
            }
    }

    // Calculate total spending from transactions
    val totalSpent = transactions.sumOf { it.amount }
    val monthlyBudget = 3000.0
    val budgetProgress = (totalSpent / monthlyBudget).coerceIn(0.0, 1.0).toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    "Good afternoon,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Alert Banner
//        item {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp),
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color.Transparent
//                )
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(
//                            brush = Brush.horizontalGradient(
//                                colors = listOf(
//                                    Color(0xFF3b82f6),
//                                    Color(0xFF8b5cf6)
//                                )
//                            )
//                        )
//                        .padding(16.dp)
//                ) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.CardGiftcard,
//                            contentDescription = null,
//                            tint = Color.White
//                        )
//                        Column {
//                            Text(
//                                "2,500 points expiring Dec 31",
//                                color = Color.White,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                            TextButton(
//                                onClick = { onNavigate(3) },
//                                colors = ButtonDefaults.textButtonColors(
//                                    contentColor = Color.White
//                                )
//                            ) {
//                                Text("Redeem now")
//                            }
//                        }
//                    }
//                }
//            }
//        }

        // Quick Stats
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.CreditCard,
                    iconColor = Color(0xFF3b82f6),
                    label = "Total Spent",
                    value = formatCurrency(totalSpent, currencyCode),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.Receipt,
                    iconColor = Color(0xFF8b5cf6),
                    label = "Transactions",
                    value = "${transactions.size}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Cards Carousel
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
                        "My Cards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigate(1) }) {
                        Text("See all")
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cards) { card ->
                        CreditCardItem(card, currencyCode, onClick = { onNavigate(1) })
                    }
                }
            }
        }

        // Recent Transactions
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
                    TextButton(onClick = { onNavigate(2) }) {
                        Text("See all")
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "No transactions yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Transactions from SMS will appear here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column {
                            transactions.take(5).forEach { transaction ->
                                TransactionItem(transaction, currencyCode)
                                if (transaction != transactions.take(5).last()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Filled.CreditCard,
                        label = "Cards",
                        color = Color(0xFF3b82f6),
                        onClick = { onNavigate(1) },
                        modifier = Modifier.weight(1f)
                    )
//                    QuickActionButton(
//                        icon = Icons.Filled.CardGiftcard,
//                        label = "Offers",
//                        color = Color(0xFF8b5cf6),
//                        onClick = { onNavigate(3) },
//                        modifier = Modifier.weight(1f)
//                    )
//                    QuickActionButton(
//                        icon = Icons.Filled.Flight,
//                        label = "Travel",
//                        color = Color(0xFFf97316),
//                        onClick = { onNavigate(4) },
//                        modifier = Modifier.weight(1f)
//                    )
                    QuickActionButton(
                        icon = Icons.Filled.TrendingUp,
                        label = "Stats",
                        color = Color(0xFF10b981),
                        onClick = { onNavigate(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Spending Overview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "This Month",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { onNavigate(3) }) {
                            Text("Details")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Spent",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${formatCurrency(totalSpent, currencyCode)} of ${formatCurrency(monthlyBudget, currencyCode)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { budgetProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (budgetProgress > 0.9f) Color(0xFFef4444) else Color(0xFF3b82f6),
                        trackColor = Color(0xFFe2e8f0)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val remaining = monthlyBudget - totalSpent
                    Text(
                        if (remaining > 0) "${formatCurrency(remaining, currencyCode)} remaining in budget"
                        else "${formatCurrency(-remaining, currencyCode)} over budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant
                               else Color(0xFFef4444)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(8.dp),
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CreditCardItem(card: CreditCard, currencyCode: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = card.gradient
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            card.name,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "•••• ${card.last4}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.CreditCard,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Balance",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                             card.balance.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "of ${formatCurrency(card.limit.toDouble(), currencyCode)}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun TransactionItem(transaction: TransactionEntry, currencyCode: String) {
    // Get emoji based on category
    val emoji = when (transaction.category.uppercase()) {
        "FOOD" -> "🍔"
        "SHOPPING" -> "🛍️"
        "BILLS" -> "📄"
        "TRAVEL" -> "✈️"
        "ENTERTAINMENT" -> "🎬"
        else -> "💳"
    }

    // Get color based on category
    val categoryColor = when (transaction.category.uppercase()) {
        "FOOD" -> Color(0xFFf97316)
        "SHOPPING" -> Color(0xFF8b5cf6)
        "BILLS" -> Color(0xFF06b6d4)
        "TRAVEL" -> Color(0xFF3b82f6)
        "ENTERTAINMENT" -> Color(0xFFec4899)
        else -> Color(0xFF64748b)
    }

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
            color = categoryColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.merchant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                formatTimestamp(transaction.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatCurrency(transaction.amount, currencyCode),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                transaction.category,
                style = MaterialTheme.typography.labelSmall,
                color = categoryColor
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun formatTimestamp(instant: Instant): String {
    // Simple formatting - you can enhance this
    return instant.toString().substringBefore('T')
}



@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
