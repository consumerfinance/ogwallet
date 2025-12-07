package dev.consumerfinance.ogwallet.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CreditCardData(
    val name: String,
    val last4: String,
    val balance: Double,
    val limit: Int,
    val gradient: List<Color>
)

data class Transaction(
    val id: Int,
    val merchant: String,
    val amount: Double,
    val date: String,
    val category: String,
    val emoji: String
)

@Composable
fun DashboardScreen(onNavigate: (Int) -> Unit) {
    val cards = listOf(
        CreditCardData(
            "Chase Sapphire Reserve",
            "4829",
            1234.56,
            10000,
            listOf(Color(0xFF334155), Color(0xFF0f172a))
        ),
        CreditCardData(
            "Amex Gold",
            "8901",
            456.78,
            15000,
            listOf(Color(0xFFf59e0b), Color(0xFFb45309))
        )
    )

    val transactions = listOf(
        Transaction(1, "Starbucks", 5.45, "Today", "Dining", "☕"),
        Transaction(2, "Amazon", 89.99, "Yesterday", "Shopping", "📦"),
        Transaction(3, "Shell Gas", 52.00, "Dec 4", "Transportation", "⛽"),
        Transaction(4, "Whole Foods", 124.32, "Dec 4", "Groceries", "🛒")
    )

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
                    "John Doe",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Alert Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF3b82f6),
                                    Color(0xFF8b5cf6)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Column {
                            Text(
                                "2,500 points expiring Dec 31",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(
                                onClick = { onNavigate(3) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Redeem now")
                            }
                        }
                    }
                }
            }
        }

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
                    label = "Total Balance",
                    value = "$1,691.34",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.CardGiftcard,
                    iconColor = Color(0xFF8b5cf6),
                    label = "Rewards",
                    value = "24,582 pts",
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
                        CreditCardItem(card, onClick = { onNavigate(1) })
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
                    TextButton(onClick = { onNavigate(5) }) {
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
                    Column {
                        transactions.forEach { transaction ->
                            TransactionItem(transaction)
                            if (transaction != transactions.last()) {
                                HorizontalDivider()
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
                    QuickActionButton(
                        icon = Icons.Filled.CardGiftcard,
                        label = "Offers",
                        color = Color(0xFF8b5cf6),
                        onClick = { onNavigate(3) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Flight,
                        label = "Travel",
                        color = Color(0xFFf97316),
                        onClick = { onNavigate(4) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Filled.TrendingUp,
                        label = "Stats",
                        color = Color(0xFF10b981),
                        onClick = { onNavigate(5) },
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
                        TextButton(onClick = { onNavigate(5) }) {
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
                            "$2,847 of $3,000",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { 0.95f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF3b82f6),
                        trackColor = Color(0xFFe2e8f0)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "$153 remaining in budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun CreditCardItem(card: CreditCardData, onClick: () -> Unit) {
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
                            "$${String.format("%.2f", card.balance)}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "of $${card.limit.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
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
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    transaction.emoji,
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
                transaction.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                transaction.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
