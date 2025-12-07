package dev.consumerfinance.ogwallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CreditCard(
    val id: String,
    val name: String,
    val cardNumber: String,
    val last4: String,
    val cvv: String,
    val expiry: String,
    val balance: Double,
    val limit: Int,
    val availableCredit: Double,
    val gradient: List<Color>,
    val network: String,
    val nextPayment: String?,
    val minPayment: Double
)

@Composable
fun CreditCardsScreen() {
    var showCardNumbers by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
    
    val cards = listOf(
        CreditCard(
            "1", "Chase Sapphire Reserve", "4532 8901 2345 4829", "4829",
            "123", "08/27", 1234.56, 10000, 8765.44,
            listOf(Color(0xFF334155), Color(0xFF0f172a)), "Visa",
            "Dec 15", 25.00
        ),
        CreditCard(
            "2", "American Express Gold", "3782 822463 18901", "8901",
            "456", "12/26", 456.78, 15000, 14543.22,
            listOf(Color(0xFFf59e0b), Color(0xFFb45309)), "Amex",
            "Dec 20", 25.00
        ),
        CreditCard(
            "3", "Citi Double Cash", "5412 7534 9012 3456", "3456",
            "789", "03/28", 0.0, 8000, 8000.0,
            listOf(Color(0xFF2563eb), Color(0xFF1e40af)), "Mastercard",
            null, 0.0
        )
    )

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Credit Cards",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Manage your credit cards",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null)
                    }
                }
            }
        }

        // Summary Card
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
                        Text(
                            "Total Available Credit",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "$31,308.66",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "Total Balance",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        "$1,691.34",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "Utilization",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        "5.1%",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Credit Cards List
        items(cards) { card ->
            CreditCardDetailItem(
                card = card,
                isNumberVisible = showCardNumbers[card.id] ?: false,
                onToggleVisibility = {
                    showCardNumbers = showCardNumbers.toMutableMap().apply {
                        this[card.id] = !(this[card.id] ?: false)
                    }
                }
            )
        }

        // Add Card Button
        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                onClick = { }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "+ ",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Add New Card",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun CreditCardDetailItem(
    card: CreditCard,
    isNumberVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Card Visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = card.gradient)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                card.network,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                card.name,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                imageVector = if (isNumberVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    
                    Text(
                        if (isNumberVisible) card.cardNumber else "•••• •••• •••• ${card.last4}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Expires",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                card.expiry,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column {
                            Text(
                                "CVV",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                if (isNumberVisible) card.cvv else "•••",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Card Details
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("Balance", "$${String.format("%.2f", card.balance)}")
                DetailRow("Available", "$${card.availableCredit.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}")
                DetailRow("Credit Limit", "$${card.limit.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}")
                
                if (card.nextPayment != null) {
                    HorizontalDivider()
                    DetailRow("Next Payment", card.nextPayment)
                    DetailRow("Minimum Due", "$${String.format("%.2f", card.minPayment)}")
                    
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Make Payment")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lock Card")
                    }
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Details")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
