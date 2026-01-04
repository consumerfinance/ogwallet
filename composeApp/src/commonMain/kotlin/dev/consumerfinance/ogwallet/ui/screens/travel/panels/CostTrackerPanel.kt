package dev.consumerfinance.ogwallet.ui.screens.travel.panels

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.db.TripRepository
import dev.consumerfinance.ogwallet.models.travel.CostCategory
import dev.consumerfinance.ogwallet.models.travel.CostItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostTrackerPanel(
    tripId: String,
    tripRepository: TripRepository
) {
    val scope = rememberCoroutineScope()
    var costs by remember { mutableStateOf(emptyList<CostItem>()) }

    // Load costs from database
    LaunchedEffect(tripId) {
        tripRepository.getTripCostItems(tripId).collect { loadedCosts ->
            costs = loadedCosts
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CostCategory?>(null) }

    // Calculate totals
    val totalCost = costs.sumOf { it.amount }
    val flightsCost = costs.filter { it.category == CostCategory.FLIGHT }.sumOf { it.amount }
    val hotelsCost = costs.filter { it.category == CostCategory.HOTEL }.sumOf { it.amount }
    val railwayCost = costs.filter { it.category == CostCategory.RAILWAY }.sumOf { it.amount }
    val otherCost = costs.filter { it.category == CostCategory.OTHER }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Cost Tracker",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Track your travel expenses in Indian Rupees",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total cost card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total Budget",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "₹${String.format("%,.2f", totalCost)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category breakdown
        Text(
            text = "Category Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                category = CostCategory.FLIGHT,
                amount = flightsCost,
                isSelected = selectedCategory == CostCategory.FLIGHT,
                onClick = {
                    selectedCategory = if (selectedCategory == CostCategory.FLIGHT) null
                    else CostCategory.FLIGHT
                },
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                category = CostCategory.HOTEL,
                amount = hotelsCost,
                isSelected = selectedCategory == CostCategory.HOTEL,
                onClick = {
                    selectedCategory = if (selectedCategory == CostCategory.HOTEL) null
                    else CostCategory.HOTEL
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                category = CostCategory.RAILWAY,
                amount = railwayCost,
                isSelected = selectedCategory == CostCategory.RAILWAY,
                onClick = {
                    selectedCategory = if (selectedCategory == CostCategory.RAILWAY) null
                    else CostCategory.RAILWAY
                },
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                category = CostCategory.OTHER,
                amount = otherCost,
                isSelected = selectedCategory == CostCategory.OTHER,
                onClick = {
                    selectedCategory = if (selectedCategory == CostCategory.OTHER) null
                    else CostCategory.OTHER
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expenses list
        Text(
            text = if (selectedCategory == null) "All Expenses" else "${selectedCategory!!.categoryDisplayName} Expenses",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filtered costs list
        val filteredCosts = if (selectedCategory == null) costs
        else costs.filter { it.category == selectedCategory }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCosts) { cost ->
                CostItemRow(
                    cost = cost,
                    onDelete = {
                        scope.launch {
                            tripRepository.deleteTripCostItem(tripId, cost.id)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Expense")
        }
    }

    if (showAddDialog) {
        AddCostDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { category, description, amount ->
                val newCostItem = CostItem(
                    id = System.currentTimeMillis().toString(),
                    category = category,
                    description = description,
                    amount = amount
                )
                scope.launch {
                    tripRepository.addTripCostItem(tripId, newCostItem)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CategoryChip(
    category: CostCategory,
    amount: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.categoryIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.categoryDisplayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "₹${String.format("%,.0f", amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CostItemRow(
    cost: CostItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Icon(
                imageVector = cost.category.categoryIcon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cost.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = cost.category.categoryDisplayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%,.0f", cost.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCostDialog(
    onDismiss: () -> Unit,
    onAdd: (CostCategory, String, Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(CostCategory.FLIGHT) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Expense",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.categoryDisplayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        CostCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = category.categoryIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(category.categoryDisplayName)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g., Flight tickets") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = it
                        }
                    },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Text(
                            "₹",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (description.isNotBlank() && amountValue > 0) {
                        onAdd(selectedCategory, description, amountValue)
                    }
                },
                enabled = description.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
