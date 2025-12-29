package dev.consumerfinance.ogwallet.ui.screens.cc


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.models.CreditCard
import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.db.CreditCardRepository
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import dev.consumerfinance.ogwallet.utils.formatCurrency
import dev.consumerfinance.ogwallet.db.DatabaseManager
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch


@Preview
@Composable
fun CreditCardsScreen() {
    val transactionRepository: TransactionRepository = koinInject()
    val creditCardRepository: CreditCardRepository = koinInject()
    val dbManager: DatabaseManager = koinInject()
    val transactions by transactionRepository.getAllTransactions().collectAsState(initial = emptyList())
    val manualCards by creditCardRepository.getAllCards().collectAsState(initial = emptyList())
    val currencyCode by dbManager.getCurrencyCode().collectAsState(initial = "INR")

    var showCardNumbers by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Group transactions by card handle and calculate balances
    val cardBalances = transactions.groupBy { it.cardHandle }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }

    // Update manual card balances based on transactions
    LaunchedEffect(transactions, manualCards) {
        manualCards.forEach { card ->
            val balance = cardBalances[card.last4] ?: 0.0
            if (card.balance != balance) {
                creditCardRepository.updateCardBalance(card.id, balance)
            }
        }
    }

    // Combine manual cards and transaction-derived cards
    val allCards = remember(manualCards, cardBalances) {
        val manualCardMap = manualCards.associateBy { it.last4 }
        val transactionCards = cardBalances.entries.mapIndexed { index, (cardHandle, balance) ->
            if (manualCardMap.containsKey(cardHandle)) {
                // Update existing manual card with calculated balance
                manualCardMap[cardHandle]!!.copy(balance = balance, availableCredit = manualCardMap[cardHandle]!!.limit - balance)
            } else {
                // Create transaction-derived card
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
                    nickname = null,
                    cardNumber = "•••• •••• •••• $cardHandle",
                    last4 = cardHandle,
                    cvv = "•••",
                    expiry = "••/••",
                    balance = balance,
                    limit = 10000,
                    availableCredit = 10000 - balance,
                    gradient = gradients[index % gradients.size],
                    network = "Card",
                    bankName = "Unknown",
                    nextPayment = null,
                    minPayment = balance * 0.02
                )
            }
        }
        (manualCards + transactionCards).distinctBy { it.id }
    }

    val totalAvailableCredit = allCards.sumOf { it.availableCredit }
    val totalBalance = allCards.sumOf { it.balance }

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
                            formatCurrency(totalBalance, currencyCode),
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
                                        "${allCards.size}",
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
        
            if (showAddCardDialog) {
                AddCardDialog(
                    onDismiss = { showAddCardDialog = false },
                    onAddCard = { card ->
                        scope.launch {
                            creditCardRepository.addCard(card)
                            showAddCardDialog = false
                        }
                    }
                )
            }

            showEditNicknameDialog?.let { cardId ->
                val card = allCards.find { it.id == cardId }
                if (card != null) {
                    EditNicknameDialog(
                        currentNickname = card.nickname,
                        onDismiss = { showEditNicknameDialog = null },
                        onSave = { nickname ->
                            scope.launch {
                                creditCardRepository.updateNickname(cardId, nickname.takeIf { it.isNotBlank() })
                                showEditNicknameDialog = null
                            }
                        }
                    )
                }
            }
        }

        // Credit Cards List
        if (allCards.isEmpty()) {
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
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        @Composable
                        fun EditNicknameDialog(
                            currentNickname: String?,
                            onDismiss: () -> Unit,
                            onSave: (String) -> Unit
                        ) {
                            var nickname by remember { mutableStateOf(currentNickname ?: "") }
                        
                            AlertDialog(
                                onDismissRequest = onDismiss,
                                title = { Text("Edit Nickname") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        OutlinedTextField(
                                            value = nickname,
                                            onValueChange = { nickname = it },
                                            label = { Text("Nickname (optional)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("Enter a nickname for this card") }
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { onSave(nickname) }
                                    ) {
                                        Text("Save")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = onDismiss) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        } else {
            items(allCards) { card ->
                CreditCardDetailItem(
                    card = card,
                    currencyCode = currencyCode,
                    isNumberVisible = showCardNumbers[card.id] ?: false,
                    onToggleVisibility = {
                        showCardNumbers = showCardNumbers.toMutableMap().apply {
                            this[card.id] = !(this[card.id] ?: false)
                        }
                    },
                    onEditNickname = { showEditNicknameDialog = card.id }
                )
            }
        }
    }
}

@Composable
fun CreditCardDetailItem(
    card: CreditCard,
    currencyCode: String,
    isNumberVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onEditNickname: () -> Unit
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
                                card.bankName,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                card.nickname ?: card.name,
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
                DetailRow("Balance", formatCurrency(card.balance, currencyCode))
                DetailRow("Available", formatCurrency(card.availableCredit, currencyCode))
                DetailRow("Credit Limit", formatCurrency(card.limit.toDouble(), currencyCode))

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
                        onClick = onEditNickname,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Edit Nickname")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
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

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onAddCard: (CreditCard) -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("10000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Card") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = last4,
                    onValueChange = { last4 = it },
                    label = { Text("Last 4 Digits") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("Credit Limit") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cardName.isNotBlank() && bankName.isNotBlank() && last4.length == 4 && creditLimit.toDoubleOrNull() != null) {
                        val limit = creditLimit.toDouble()
                        val gradients = listOf(
                            listOf(Color(0xFF334155), Color(0xFF0f172a)),
                            listOf(Color(0xFFf59e0b), Color(0xFFb45309)),
                            listOf(Color(0xFF2563eb), Color(0xFF1e40af)),
                            listOf(Color(0xFF8b5cf6), Color(0xFF6366f1)),
                            listOf(Color(0xFF10b981), Color(0xFF059669))
                        )
                        val card = CreditCard(
                            id = last4,
                            name = cardName,
                            nickname = null,
                            cardNumber = "•••• •••• •••• $last4",
                            last4 = last4,
                            cvv = "•••",
                            expiry = "••/••",
                            balance = 0.0,
                            limit = limit.toInt(),
                            availableCredit = limit,
                            gradient = gradients.random(),
                            network = "Card",
                            bankName = bankName,
                            nextPayment = null,
                            minPayment = 0.0
                        )
                        onAddCard(card)
                    }
                }
            ) {
                Text("Add Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
