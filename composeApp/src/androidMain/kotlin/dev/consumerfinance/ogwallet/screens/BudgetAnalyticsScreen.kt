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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SpendingCategory(
    val name: String,
    val amount: Int,
    val budget: Int,
    val color: Color
)

data class Alert(
    val id: Int,
    val category: String,
    val message: String,
    val severity: String
)

@Composable
fun BudgetAnalyticsScreen() {
    var timeRange by remember { mutableStateOf("month") }

    val spendingByCategory = listOf(
        SpendingCategory("Dining", 845, 800, Color(0xFFf97316)),
        SpendingCategory("Groceries", 654, 700, Color(0xFF10b981)),
        SpendingCategory("Transport", 432, 500, Color(0xFF3b82f6)),
        SpendingCategory("Shopping", 523, 400, Color(0xFF8b5cf6)),
        SpendingCategory("Entertainment", 234, 300, Color(0xFFec4899)),
        SpendingCategory("Utilities", 159, 200, Color(0xFF6366f1))
    )

    val totalSpent = spendingByCategory.sumOf { it.amount }
    val totalBudget = spendingByCategory.sumOf { it.budget }
    val budgetRemaining = totalBudget - totalSpent

    val alerts = listOf(
        Alert(1, "Dining", "Exceeded dining budget by $45", "high"),
        Alert(2, "Shopping", "Shopping is 131% of budget", "high")
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
                        text = "Week",
                        selected = timeRange == "week",
                        onClick = { timeRange = "week" },
                        modifier = Modifier.weight(1f)
                    )
                    TimeRangeButton(
                        text = "Month",
                        selected = timeRange == "month",
                        onClick = { timeRange = "month" },
                        modifier = Modifier.weight(1f)
                    )
                    TimeRangeButton(
                        text = "Year",
                        selected = timeRange == "year",
                        onClick = { timeRange = "year" },
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
                        value = "$$totalSpent"
                    )
                    StatCard(
                        icon = Icons.Filled.TrendingUp,
                        iconColor = Color(0xFF10b981),
                        label = "Remaining",
                        value = "$$budgetRemaining"
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
                        value = "$$totalBudget"
                    )
                    StatCard(
                        icon = Icons.Filled.CalendarToday,
                        iconColor = Color(0xFF8b5cf6),
                        label = "Daily Avg",
                        value = "$${String.format("%.2f", totalSpent / 6.0)}"
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
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "$${category.amount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
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
                            CategoryBudgetItem(category)
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
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
fun CategoryBudgetItem(category: SpendingCategory) {
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
                    "$${category.amount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) Color(0xFFef4444) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "/ $${category.budget}",
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
