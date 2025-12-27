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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.models.budget.Alert
import dev.consumerfinance.ogwallet.models.transactions.SpendingCategory
import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.utils.formatCurrency
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

// Helper function to get category colors
private fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "FOOD" -> Color(0xFFf97316)
        "SHOPPING" -> Color(0xFF8b5cf6)
        "BILLS" -> Color(0xFF06b6d4)
        "TRAVEL" -> Color(0xFF3b82f6)
        "ENTERTAINMENT" -> Color(0xFFec4899)
        else -> Color(0xFF64748b)
    }
}

@Preview
@Composable
fun BudgetAnalyticsScreen() {
    val repository: TransactionRepository = koinInject()
    val dbManager: DatabaseManager = koinInject()
    var timeRange by remember { mutableStateOf("month") } // Moved to before usage
    val transactions by repository.getTransactionsByTimeRange(timeRange).collectAsState(initial = emptyList())
    val categoryBreakdown by repository.getSpendingBreakdownByTimeRange(timeRange).collectAsState(initial = emptyList())
    val currencyCode by dbManager.getCurrencyCode().collectAsState(initial = "USD")

    // Define budgets for each category
    val categoryBudgets = mapOf(
        "FOOD" to 800,
        "SHOPPING" to 400,
        "BILLS" to 500,
        "TRAVEL" to 1000,
        "ENTERTAINMENT" to 300,
        "OTHER" to 200
    )

    // Map real data to SpendingCategory with budgets
    val spendingByCategory = categoryBreakdown.map { summary ->
        val budget = categoryBudgets[summary.category.uppercase()] ?: 500
        SpendingCategory(
            name = summary.category,
            amount = summary.totalAmount.toInt(),
            budget = budget,
            color = getCategoryColor(summary.category)
        )
    }

    val totalSpent = spendingByCategory.sumOf { it.amount }
    val totalBudget = spendingByCategory.sumOf { it.budget }
    val budgetRemaining = totalBudget - totalSpent

    // Generate alerts for categories over budget
    val alerts = spendingByCategory.filter { it.amount > it.budget }.map { category ->
        val overage = category.amount - category.budget
        Alert(
            id = category.name.hashCode(),
            category = category.name,
            message = "Exceeded ${category.name} budget by ${formatCurrency(overage.toDouble(), currencyCode)}",
            severity = "high"
        )
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
                    "Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Track spending and budget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Range Selector
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFe2e8f0)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimeRangeButton(
                        text = "Monthly",
                        selected = timeRange == "month",
                        onClick = { timeRange = "month" },
                        modifier = Modifier.weight(1f)
                    )
                    TimeRangeButton(
                        text = "Yearly",
                        selected = timeRange == "year",
                        onClick = { timeRange = "year" },
                        modifier = Modifier.weight(1f)
                    )
                    TimeRangeButton(
                        text = "Weekly",
                        selected = timeRange == "week",
                        onClick = { timeRange = "week" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Stats Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Filled.TrendingDown,
                        iconColor = Color(0xFFef4444),
                        label = "Total Spent",
                        value = formatCurrency(totalSpent.toDouble(), currencyCode)
                    )
                    StatCard(
                        icon = Icons.Filled.TrendingUp,
                        iconColor = if (budgetRemaining >= 0) Color(0xFF10b981) else Color(0xFFef4444),
                        label = "Remaining",
                        value = formatCurrency(if (budgetRemaining >= 0) budgetRemaining.toDouble() else -budgetRemaining.toDouble(), currencyCode)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Filled.Payments,
                        iconColor = Color(0xFF3b82f6),
                        label = "Budget",
                        value = formatCurrency(totalBudget.toDouble(), currencyCode)
                    )
                    StatCard(
                        icon = Icons.Filled.Receipt,
                        iconColor = Color(0xFF8b5cf6),
                        label = "Transactions",
                        value = "${transactions.size}"
                    )
                }
            }
        }

        // Alerts
        if (alerts.isNotEmpty()) {
            items(alerts) { alert ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFfee2e2)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color(0xFFef4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                alert.category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                alert.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Spending by Category (Pie Chart Placeholder)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Spending by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Simple legend instead of actual pie chart
                    if (spendingByCategory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No spending data yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            spendingByCategory.forEach { category ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(12.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = category.color
                                    ) {}
                                    Text(
                                        category.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (category.name.uppercase() == "OTHER") Color.Black else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        formatCurrency(category.amount.toDouble(), currencyCode),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Budget vs Actual
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Budget vs Actual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        spendingByCategory.forEach { category ->
                            CategoryBudgetItem(category, currencyCode)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF10b981),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                "Insight",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Spending is down 12% compared to last month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRangeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.White else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = if (selected) ButtonDefaults.buttonElevation(2.dp) else ButtonDefaults.buttonElevation(0.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color.Black)
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Card(
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                style = MaterialTheme.typography.labelSmall,
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
fun CategoryBudgetItem(category: SpendingCategory, currencyCode: String) {
    val percentage = (category.amount.toFloat() / category.budget.toFloat() * 100).toInt()
    val isOverBudget = percentage > 100

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    formatCurrency(category.amount.toDouble(), currencyCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) Color(0xFFef4444) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "/ ${formatCurrency(category.budget.toDouble(), currencyCode)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { (percentage.coerceAtMost(100) / 100f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isOverBudget) Color(0xFFef4444) else Color(0xFF3b82f6),
            trackColor = Color(0xFFe2e8f0)
        )
    }
}
