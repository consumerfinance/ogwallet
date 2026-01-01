package dev.consumerfinance.ogwallet.ui.screens.travel.panels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CreditCardReward(
    val id: String,
    val bankName: String,
    val cardName: String,
    val rewardRate: String,
    val benefits: List<String>,
    val bestFor: String,
    val fees: String,
    val color: Color
)

@Composable
fun CreditCardRewardsPanel() {
    val creditCards = remember {
        listOf(
            CreditCardReward(
                id = "1",
                bankName = "HDFC Bank",
                cardName = "Diners Club Black",
                rewardRate = "10X Reward Points",
                benefits = listOf(
                    "10 reward points per ₹150 on travel bookings",
                    "Up to 33% value on flights & hotels via SmartBuy",
                    "2 complimentary domestic lounge visits per quarter",
                    "1% fuel surcharge waiver",
                    "Golf privileges and priority pass"
                ),
                bestFor = "Premium travelers who book online",
                fees = "₹10,000/year (Waived on ₹5L spend)",
                color = Color(0xFF004C8F)
            ),
            CreditCardReward(
                id = "2",
                bankName = "Axis Bank",
                cardName = "Magnus",
                rewardRate = "25 Edge Miles per ₹200",
                benefits = listOf(
                    "12 Edge Miles per ₹200 base rate",
                    "Bonus miles on milestone spends",
                    "Complimentary airport lounge access (unlimited)",
                    "Travel insurance up to ₹50 lakhs",
                    "Priority customer service"
                ),
                bestFor = "Frequent flyers and high spenders",
                fees = "₹12,500/year (Waived on ₹15L spend)",
                color = Color(0xFF97144D)
            ),
            CreditCardReward(
                id = "3",
                bankName = "SBI Card",
                cardName = "Elite",
                rewardRate = "5% Cashback",
                benefits = listOf(
                    "5% cashback on travel bookings",
                    "Railway lounge access (4 times per year)",
                    "Exclusive movie ticket offers",
                    "Fuel surcharge waiver (1%)",
                    "Lost card liability cover"
                ),
                bestFor = "Value-conscious travelers",
                fees = "₹4,999/year (Waived on ₹2L spend)",
                color = Color(0xFF1E3A8A)
            ),
            CreditCardReward(
                id = "4",
                bankName = "ICICI Bank",
                cardName = "Sapphiro",
                rewardRate = "2 Reward Points per ₹100",
                benefits = listOf(
                    "Complimentary airport lounge access (8/year)",
                    "Travel & Air Accident cover up to ₹3 Crores",
                    "Dining privileges at partner restaurants",
                    "Movie ticket discounts",
                    "Reward point redemption for travel"
                ),
                bestFor = "Mid-tier premium travelers",
                fees = "₹3,500/year (Waived on ₹3L spend)",
                color = Color(0xFFB45F06)
            ),
            CreditCardReward(
                id = "5",
                bankName = "Yes Bank",
                cardName = "Marquee",
                rewardRate = "6 Reward Points per ₹100",
                benefits = listOf(
                    "High reward rate on all spends",
                    "Complimentary Priority Pass",
                    "Golf games at premium courses",
                    "Concierge services",
                    "Travel insurance coverage"
                ),
                bestFor = "Luxury travel enthusiasts",
                fees = "₹10,000/year (Waived on ₹8L spend)",
                color = Color(0xFF1A5F7A)
            )
        )
    }

    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Credit Card Rewards",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Maximize rewards on your travel bookings with these Indian credit cards",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Pro Tip: Use travel portals linked to your cards for maximum rewards!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "All",
                onClick = { selectedFilter = "All" },
                label = { Text("All Cards") },
                leadingIcon = if (selectedFilter == "All") {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
            FilterChip(
                selected = selectedFilter == "Premium",
                onClick = { selectedFilter = "Premium" },
                label = { Text("Premium") }
            )
            FilterChip(
                selected = selectedFilter == "Value",
                onClick = { selectedFilter = "Value" },
                label = { Text("Value") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cards list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(creditCards) { card ->
                CreditCardItem(
                    card = card,
                    isExpanded = expandedCardId == card.id,
                    onExpandChange = {
                        expandedCardId = if (expandedCardId == card.id) null else card.id
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardItem(
    card: CreditCardReward,
    isExpanded: Boolean,
    onExpandChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onExpandChange,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        border = BorderStroke(1.dp, card.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.bankName,
                        style = MaterialTheme.typography.labelLarge,
                        color = card.color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.cardName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reward rate badge
            Surface(
                color = card.color.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = card.color
                    )
                    Text(
                        text = card.rewardRate,
                        style = MaterialTheme.typography.labelLarge,
                        color = card.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expanded content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // Best for
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Best for: ${card.bestFor}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(12.dp))

                    // Benefits
                    Text(
                        text = "Key Benefits",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    card.benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fees
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Column {
                            Text(
                                text = "Annual Fee",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = card.fees,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
