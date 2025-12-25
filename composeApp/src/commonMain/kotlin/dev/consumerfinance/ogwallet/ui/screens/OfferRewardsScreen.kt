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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import dev.consumerfinance.ogwallet.data.CreditCardDataLoader

data class RewardsBalance(
    val points: Int,
    val cashBack: Double,
    val expiringPoints: Int,
    val expiryDate: String
)

data class Offer(
    val id: String,
    val title: String,
    val description: String,
    val expiry: String,
    val category: String,
    val emoji: String,
    val gradient: List<Color>
)

data class RedemptionOption(
    val id: String,
    val name: String,
    val rate: String,
    val minimum: Int,
    val emoji: String,
    val available: Boolean
)

data class CategoryEarning(
    val category: String,
    val points: Int,
    val percentage: Int,
    val color: Color
)

/**
 * Convert hex color string to Compose Color
 */
fun hexToColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return Color(("FF" + cleanHex).toLong(16))
}

@Preview
@Composable
fun OffersRewardsScreen() {
    // Load credit card data from git submodule
    val creditCardData = remember { CreditCardDataLoader.loadData() }

    val rewardsBalance = RewardsBalance(
        points = 24582,
        cashBack = 245.82,
        expiringPoints = 2500,
        expiryDate = "Dec 31"
    )

    // Convert loaded data to UI models
    val activeOffers = creditCardData.offers
        .filter { it.isActive }
        .map { offerData ->
            Offer(
                id = offerData.id,
                title = offerData.title,
                description = offerData.description,
                expiry = offerData.expiryDate ?: "No expiry",
                category = offerData.category.replaceFirstChar { it.uppercase() },
                emoji = offerData.emoji,
                gradient = offerData.gradientColors.map { hexToColor(it) }
            )
        }

    val redeemOptions = creditCardData.redemptionOptions.map { redemptionData ->
        RedemptionOption(
            id = redemptionData.id,
            name = redemptionData.name,
            rate = redemptionData.rate,
            minimum = redemptionData.minimumPoints,
            emoji = redemptionData.emoji,
            available = redemptionData.available
        )
    }

    val categoryEarnings = listOf(
        CategoryEarning("Dining", 8234, 33, Color(0xFFf97316)),
        CategoryEarning("Travel", 6500, 26, Color(0xFF3b82f6)),
        CategoryEarning("Shopping", 5234, 21, Color(0xFF8b5cf6)),
        CategoryEarning("Gas", 3114, 13, Color(0xFF06b6d4)),
        CategoryEarning("Other", 1500, 7, Color(0xFF64748b))
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
                    "Offers & Rewards",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Maximize your rewards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Rewards Balance
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
                                colors = listOf(Color(0xFF8b5cf6), Color(0xFFec4899))
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
                                        imageVector = Icons.Filled.CardGiftcard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "Total Points",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    rewardsBalance.points.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,"),
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

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
                                        "Cash Value",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        rewardsBalance.cashBack.toString(),
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
                                        "Expiring ${rewardsBalance.expiryDate}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        "${rewardsBalance.expiringPoints.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")} pts",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF8b5cf6)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Redeem Points")
                        }
                    }
                }
            }
        }

        // Active Offers
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
                        "Active Offers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { }) {
                        Text("See all")
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        items(activeOffers) { offer ->
            OfferCard(offer)
        }

        // Redemption Options
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Redeem Points",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        redeemOptions.forEachIndexed { index, option ->
                            RedemptionOptionItem(option)
                            if (index < redeemOptions.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        // Points by Category
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Points by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        categoryEarnings.forEach { earning ->
                            CategoryEarningItem(earning)
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
                                "This Month",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Earned 1,240 points, up 15% from last month",
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
fun OfferCard(offer: Offer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = offer.gradient)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        offer.emoji,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            offer.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            offer.description,
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Expires ${offer.expiry}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Activate", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun RedemptionOptionItem(option: RedemptionOption) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            option.emoji,
            style = MaterialTheme.typography.headlineMedium
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                option.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                option.rate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CategoryEarningItem(earning: CategoryEarning) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                earning.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${earning.points.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")} pts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { earning.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = earning.color,
            trackColor = Color(0xFFe2e8f0)
        )
    }
}
