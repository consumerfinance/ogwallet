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

data class StopoverProgram(
    val id: String,
    val airline: String,
    val programName: String,
    val destination: String,
    val minLayover: String,
    val maxLayover: String,
    val description: String,
    val benefits: List<String>,
    val restrictions: List<String>,
    val howToBook: String,
    val color: Color
)

@Composable
fun StopoverHintsPanel() {
    val stopovers = remember {
        listOf(
            StopoverProgram(
                id = "1",
                airline = "Singapore Airlines",
                programName = "Singapore Stopover Holiday",
                destination = "Singapore (SIN)",
                minLayover = "4 hours",
                maxLayover = "96 hours",
                description = "Explore the Lion City with special hotel rates and free city tours during your layover",
                benefits = listOf(
                    "Discounted hotel rates (up to 50% off)",
                    "Free Singapore Tour (6+ hour layover)",
                    "Special dining and attraction packages",
                    "Free shuttle bus to selected hotels",
                    "Singapore Stopover Pass perks"
                ),
                restrictions = listOf(
                    "Must be booked with Singapore Airlines ticket",
                    "Not available on all fare types",
                    "Minimum 4 hours layover required"
                ),
                howToBook = "Book through Singapore Airlines website or contact reservations",
                color = Color(0xFF00205B)
            ),
            StopoverProgram(
                id = "2",
                airline = "Turkish Airlines",
                programName = "Touristanbul",
                destination = "Istanbul (IST)",
                minLayover = "6 hours",
                maxLayover = "24+ hours",
                description = "Free hotel accommodation and guided city tours in Istanbul for qualifying layovers",
                benefits = listOf(
                    "Free hotel for layovers 20+ hours (economy) or 10+ hours (business)",
                    "Free guided city tours for 6+ hour layovers",
                    "Tours include Blue Mosque, Grand Bazaar, Bosphorus",
                    "Multiple tour times throughout the day",
                    "All transportation included"
                ),
                restrictions = listOf(
                    "International to international connections only",
                    "Not available on all tickets",
                    "Subject to availability"
                ),
                howToBook = "Apply at Turkish Airlines transfer desk at Istanbul Airport",
                color = Color(0xFFC70A0C)
            ),
            StopoverProgram(
                id = "3",
                airline = "Icelandair",
                programName = "Iceland Stopover",
                destination = "Reykjavik (KEF)",
                minLayover = "1 day",
                maxLayover = "7 days",
                description = "Stay in Iceland for up to 7 days at no additional airfare between North America and Europe",
                benefits = listOf(
                    "No extra airfare for stopover up to 7 days",
                    "Special hotel and car rental packages",
                    "Guided tour discounts",
                    "Blue Lagoon packages",
                    "Northern Lights tours (seasonal)"
                ),
                restrictions = listOf(
                    "Available on transatlantic routes only",
                    "Must be booked before initial departure",
                    "Some blackout dates apply"
                ),
                howToBook = "Select stopover option when booking on Icelandair.com",
                color = Color(0xFF00AEEF)
            ),
            StopoverProgram(
                id = "4",
                airline = "Qatar Airways",
                programName = "Qatar Stopover",
                destination = "Doha (DOH)",
                minLayover = "1 night",
                maxLayover = "4 nights",
                description = "Discover Doha with discounted hotel packages and city experiences",
                benefits = listOf(
                    "Discounted 4 & 5-star hotel rates",
                    "Free Doha city tours",
                    "Desert safari packages",
                    "Museum and attraction discounts",
                    "Airport meet & greet service"
                ),
                restrictions = listOf(
                    "Must book stopover package in advance",
                    "Minimum stay requirements vary",
                    "Limited availability during peak seasons"
                ),
                howToBook = "Book via Qatar Airways Stopover Packages page",
                color = Color(0xFF5C0C33)
            ),
            StopoverProgram(
                id = "5",
                airline = "Emirates",
                programName = "Dubai Connect",
                destination = "Dubai (DXB)",
                minLayover = "8 hours",
                maxLayover = "24 hours",
                description = "Complimentary hotel accommodation and meals for Emirates passengers",
                benefits = listOf(
                    "Free hotel accommodation",
                    "Complimentary meals and refreshments",
                    "Free visa (if required)",
                    "Ground transportation included",
                    "Available for all fare classes"
                ),
                restrictions = listOf(
                    "8-24 hour connection window only",
                    "Must be Emirates or flydubai connection",
                    "Subject to eligibility"
                ),
                howToBook = "Automatically applied if eligible, or request via Emirates",
                color = Color(0xFFD71921)
            ),
            StopoverProgram(
                id = "6",
                airline = "Finnair",
                programName = "Helsinki Stopover",
                destination = "Helsinki (HEL)",
                minLayover = "5 hours",
                maxLayover = "5 days",
                description = "Experience Nordic culture with special stopover packages in Helsinki",
                benefits = listOf(
                    "Discounted hotel packages",
                    "Helsinki Card included (attractions & transport)",
                    "Sauna experience vouchers",
                    "City walking tour options",
                    "Airport lounge access for premium passengers"
                ),
                restrictions = listOf(
                    "Must book through Finnair Stopover program",
                    "Available on intercontinental routes",
                    "Advance booking required"
                ),
                howToBook = "Select Helsinki stopover when booking on Finnair.com",
                color = Color(0xFF00205B)
            )
        )
    }

    var expandedId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Stopover Programs",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Make the most of your layovers with these airline stopover programs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TravelExplore,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Column {
                    Text(
                        text = "Why Stopovers?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Visit an extra city at no additional airfare cost!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stopovers list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stopovers) { stopover ->
                StopoverCard(
                    stopover = stopover,
                    isExpanded = expandedId == stopover.id,
                    onExpandChange = {
                        expandedId = if (expandedId == stopover.id) null else stopover.id
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopoverCard(
    stopover: StopoverProgram,
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
        border = BorderStroke(2.dp, stopover.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Airline badge
                    Surface(
                        color = stopover.color,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stopover.airline,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stopover.programName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stopover.destination,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DurationChip(
                    label = "Min",
                    duration = stopover.minLayover,
                    icon = Icons.Default.Schedule
                )
                DurationChip(
                    label = "Max",
                    duration = stopover.maxLayover,
                    icon = Icons.Default.DateRange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stopover.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Expanded content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Benefits
                    Text(
                        text = "What's Included",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    stopover.benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF10B981)
                            )
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Restrictions
                    Text(
                        text = "Important Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    stopover.restrictions.forEach { restriction ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = restriction,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    // How to book
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.medium
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
                            Column {
                                Text(
                                    text = "How to Book",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = stopover.howToBook,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DurationChip(
    label: String,
    duration: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
