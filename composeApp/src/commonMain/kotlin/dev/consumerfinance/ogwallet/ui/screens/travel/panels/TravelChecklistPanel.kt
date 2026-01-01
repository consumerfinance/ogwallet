package dev.consumerfinance.ogwallet.ui.screens.travel.panels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.models.travel.ChecklistItem

@Composable
fun TravelChecklistPanel() {
    var items by remember {
        mutableStateOf(
            listOf(
                ChecklistItem("1", "Book flight tickets", false, "Travel Booking"),
                ChecklistItem("2", "Reserve hotel rooms", false, "Accommodation"),
                ChecklistItem("3", "Check visa requirements", false, "Documents"),
                ChecklistItem("4", "Get travel insurance", false, "Insurance"),
                ChecklistItem("5", "Renew passport if needed", false, "Documents"),
                ChecklistItem("6", "Make photocopies of important documents", false, "Documents"),
                ChecklistItem("7", "Pack clothes and essentials", false, "Packing"),
                ChecklistItem("8", "Pack toiletries and medications", false, "Packing"),
                ChecklistItem("9", "Notify bank about travel dates", false, "Finance"),
                ChecklistItem("10", "Download offline maps", false, "Apps & Tech"),
                ChecklistItem("11", "Install translation apps", false, "Apps & Tech"),
                ChecklistItem("12", "Create emergency contacts list", false, "Safety"),
                ChecklistItem("13", "Get vaccinations if required", false, "Health"),
                ChecklistItem("14", "Book airport transfers", false, "Transportation"),
                ChecklistItem("15", "Arrange pet/plant care", false, "Home"),
                ChecklistItem("16", "Stop mail delivery", false, "Home"),
                ChecklistItem("17", "Clean out fridge", false, "Home"),
                ChecklistItem("18", "Pack chargers and adapters", false, "Electronics"),
                ChecklistItem("19", "Download entertainment for flight", false, "Entertainment"),
                ChecklistItem("20", "Check weather forecast", false, "Planning")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var groupByCategory by remember { mutableStateOf(true) }
    var hideCompleted by remember { mutableStateOf(false) }

    // Calculate stats
    val completedCount = items.count { it.completed }
    val totalCount = items.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Travel Checklist",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Stay organized with your pre-travel tasks",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$completedCount of $totalCount completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Circular progress indicator
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(60.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 6.dp,
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = groupByCategory,
                onClick = { groupByCategory = !groupByCategory },
                label = { Text("Group by Category") },
                leadingIcon = if (groupByCategory) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )

            FilterChip(
                selected = hideCompleted,
                onClick = { hideCompleted = !hideCompleted },
                label = { Text("Hide Completed") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checklist
        val displayItems = if (hideCompleted) items.filter { !it.completed } else items

        if (groupByCategory) {
            val groupedItems = displayItems.groupBy { it.category }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedItems.forEach { (category, categoryItems) ->
                    item {
                        CategorySection(
                            category = category,
                            items = categoryItems,
                            onToggle = { id ->
                                items = items.map {
                                    if (it.id == id) it.copy(completed = !it.completed) else it
                                }
                            },
                            onDelete = { id ->
                                items = items.filter { it.id != id }
                            }
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayItems) { item ->
                    ChecklistItemRow(
                        item = item,
                        onToggle = {
                            items = items.map {
                                if (it.id == item.id) it.copy(completed = !it.completed) else it
                            }
                        },
                        onDelete = {
                            items = items.filter { it.id != item.id }
                        }
                    )
                }
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
            Text("Add Task")
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { text, category ->
                items = items + ChecklistItem(
                    id = System.currentTimeMillis().toString(),
                    text = text,
                    completed = false,
                    category = category
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CategorySection(
    category: String,
    items: List<ChecklistItem>,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val completedInCategory = items.count { it.completed }

    Column {
        // Category header
        Surface(
            onClick = { isExpanded = !isExpanded },
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedInCategory/${items.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Category items
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    ChecklistItemRow(
                        item = item,
                        onToggle = { onToggle(item.id) },
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.completed)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
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
            Checkbox(
                checked = item.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.completed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (item.completed) TextDecoration.LineThrough else null,
                    fontWeight = if (!item.completed) FontWeight.Medium else FontWeight.Normal
                )
                if (!item.completed) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Travel Booking",
        "Accommodation",
        "Documents",
        "Insurance",
        "Packing",
        "Finance",
        "Apps & Tech",
        "Safety",
        "Health",
        "Transportation",
        "Home",
        "Electronics",
        "Entertainment",
        "Planning",
        "General"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Checklist Item",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Task") },
                    placeholder = { Text("e.g., Pack sunscreen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
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
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(cat),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(cat)
                                    }
                                },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(text, category) },
                enabled = text.isNotBlank()
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

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Travel Booking" -> Icons.Default.Flight
        "Accommodation" -> Icons.Default.Hotel
        "Documents" -> Icons.Default.Description
        "Insurance" -> Icons.Default.HealthAndSafety
        "Packing" -> Icons.Default.Luggage
        "Finance" -> Icons.Default.AccountBalance
        "Apps & Tech" -> Icons.Default.Smartphone
        "Safety" -> Icons.Default.Security
        "Health" -> Icons.Default.LocalHospital
        "Transportation" -> Icons.Default.DirectionsCar
        "Home" -> Icons.Default.Home
        "Electronics" -> Icons.Default.Devices
        "Entertainment" -> Icons.Default.Movie
        "Planning" -> Icons.Default.CalendarMonth
        else -> Icons.Default.CheckCircle
    }
}
