package dev.consumerfinance.ogwallet.ui.screens


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
import dev.consumerfinance.ogwallet.models.CreditCard
import dev.consumerfinance.ogwallet.db.TransactionRepository
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import dev.consumerfinance.ogwallet.util.formatCurrency


@Preview
@Composable
fun CreditCardsScreen() {
    val repository: TransactionRepository = koinInject()
    val transactions by repository.getAllTransactions().collectAsState(initial = emptyList())

    var showCardNumbers by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }

    // Group transactions by card handle and calculate balances
    val cardBalances = transactions.groupBy { it.cardHandle }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }

    // Create cards based on unique card handles from transactions
    val cards = cardBalances.entries.mapIndexed { index, (cardHandle, balance) ->
        val gradients = listOf(
            listOf(Color(0xFF334155), Color(0xFF0f172a)),
            listOf(Color(0xFFf59e0b), Color(0xFFb45309)),
            listOf(Color(0xFF2563eb), Color(0xFF1e40af)),
            listOf(Color(0xFF8b5cf6), Color(0xFF6366f1)),
            listOf(Color(0xFF10b981), Color(0xFF059669))
        )
        CreditCard(
            id = cardHandle,
            name = "Card ending ${cardHandle}",
            cardNumber = "•••• •••• •••• $cardHandle",
            last4 = cardHandle,
            cvv = "•••",
            expiry = "••/••",
            balance = balance,
            limit = 10000,
            availableCredit = 10000 - balance,
            gradient = gradients[index % gradients.size],
            network = "Card",
            nextPayment = null,
            minPayment = balance * 0.02
        )
    }

    val totalAvailableCredit = cards.sumOf { it.availableCredit }
    val totalBalance = cards.sumOf { it.balance }

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
                            "Total Balance",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "₹${formatCurrency(totalBalance)}",
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
                                        "Cards",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        "${cards.size}",
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
                                        "Transactions",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        "${transactions.size}",
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
        if (cards.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "No cards found",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Transactions from SMS will create cards automatically",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
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
        }

        // Add Card Button
//        item {
//            OutlinedCard(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp),
//                shape = RoundedCornerShape(16.dp),
//                onClick = { }
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(24.dp),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        "+ ",
//                        style = MaterialTheme.typography.headlineSmall
//                    )
//                    Text(
//                        "Add New Card",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//            }
//        }
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
                DetailRow("Balance", card.balance.toString())
                DetailRow("Available", "$${card.availableCredit.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}")
                DetailRow("Credit Limit", "$${card.limit.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}")

                if (card.nextPayment != null) {
                    HorizontalDivider()
                    DetailRow("Next Payment", card.nextPayment)
                    DetailRow("Minimum Due", card.minPayment.toString())

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
