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
import androidx.compose.ui.unit.dp

data class Activity(
    val id: String,
    val title: String,
    val location: String,
    val description: String,
    val category: ActivityCategory,
    val rating: Float,
    val estimatedCost: String,
    val duration: String,
    val bestTime: String,
    val tips: List<String>
)

enum class ActivityCategory {
    HISTORICAL,
    NATURE,
    FOOD,
    ADVENTURE,
    SHOPPING,
    CULTURE
}

@Composable
fun ActivitiesPanel() {
    val activities = remember {
        listOf(
            Activity(
                id = "1",
                title = "Gateway of India",
                location = "Mumbai, Maharashtra",
                description = "Iconic arch monument built during the British Raj, overlooking the Arabian Sea. A must-visit landmark with historical significance.",
                category = ActivityCategory.HISTORICAL,
                rating = 4.5f,
                estimatedCost = "Free entry",
                duration = "1-2 hours",
                bestTime = "Early morning or sunset",
                tips = listOf(
                    "Take a boat ride to Elephanta Caves from here",
                    "Best photographed during golden hour",
                    "Watch out for street vendors",
                    "Combine with nearby Taj Palace visit"
                )
            ),
            Activity(
                id = "2",
                title = "Red Fort (Lal Qila)",
                location = "New Delhi",
                description = "Historic Mughal fortress and UNESCO World Heritage Site, showcasing Indo-Islamic architecture from the 17th century.",
                category = ActivityCategory.HISTORICAL,
                rating = 4.6f,
                estimatedCost = "₹35 (Indians), ₹500 (Foreigners)",
                duration = "2-3 hours",
                bestTime = "Morning (9 AM - 12 PM)",
                tips = listOf(
                    "Arrive early to avoid crowds",
                    "Light and Sound show in evening",
                    "Wear comfortable walking shoes",
                    "Photography allowed but no video inside museum"
                )
            ),
            Activity(
                id = "3",
                title = "Marine Drive",
                location = "Mumbai, Maharashtra",
                description = "Beautiful 3.6 km long boulevard along the coast, perfect for evening walks with stunning sunset views over the Arabian Sea.",
                category = ActivityCategory.NATURE,
                rating = 4.7f,
                estimatedCost = "Free",
                duration = "1-2 hours",
                bestTime = "Sunset (6-7 PM)",
                tips = listOf(
                    "Try local street food from vendors",
                    "Walk from Nariman Point to Chowpatty",
                    "Visit during monsoon for dramatic waves",
                    "Great for jogging in early morning"
                )
            ),
            Activity(
                id = "4",
                title = "Chandni Chowk Food Walk",
                location = "Old Delhi",
                description = "Experience authentic Delhi street food in one of India's oldest and busiest markets, dating back to the 17th century.",
                category = ActivityCategory.FOOD,
                rating = 4.8f,
                estimatedCost = "₹500-1000 per person",
                duration = "3-4 hours",
                bestTime = "Morning or evening",
                tips = listOf(
                    "Try parathas at Paranthe Wali Gali",
                    "Must-have: Jalebi at Old Famous Jalebi Wala",
                    "Go with an empty stomach!",
                    "Best explored on foot or cycle rickshaw"
                )
            ),
            Activity(
                id = "5",
                title = "Sanjay Gandhi National Park",
                location = "Mumbai, Maharashtra",
                description = "One of the most visited national parks in the world, featuring Kanheri Caves and diverse wildlife within city limits.",
                category = ActivityCategory.NATURE,
                rating = 4.4f,
                estimatedCost = "₹50-100 entry",
                duration = "4-5 hours",
                bestTime = "Early morning",
                tips = listOf(
                    "Visit Kanheri Caves (2000-year-old Buddhist caves)",
                    "Possible leopard sightings (from safe distance)",
                    "Carry water and snacks",
                    "Take the nature trail for bird watching"
                )
            ),
            Activity(
                id = "6",
                title = "Qutub Minar",
                location = "New Delhi",
                description = "73-meter tall UNESCO World Heritage Site, the tallest brick minaret in the world, built in 1193.",
                category = ActivityCategory.HISTORICAL,
                rating = 4.5f,
                estimatedCost = "₹30 (Indians), ₹500 (Foreigners)",
                duration = "1-2 hours",
                bestTime = "Morning or late afternoon",
                tips = listOf(
                    "Explore the entire Qutub Complex",
                    "See the Iron Pillar (rust-free for 1600+ years)",
                    "Great for photography enthusiasts",
                    "Combine with nearby Mehrauli Archaeological Park"
                )
            ),
            Activity(
                id = "7",
                title = "Colaba Causeway Shopping",
                location = "Mumbai, Maharashtra",
                description = "Vibrant shopping street offering everything from jewelry to clothing, handicrafts, and souvenirs at bargain prices.",
                category = ActivityCategory.SHOPPING,
                rating = 4.3f,
                estimatedCost = "Budget dependent",
                duration = "2-3 hours",
                bestTime = "Afternoon to evening",
                tips = listOf(
                    "Bargaining is expected and encouraged",
                    "Start at 50% of quoted price",
                    "Try Leopold Cafe for refreshments",
                    "Look for unique Indian handicrafts"
                )
            ),
            Activity(
                id = "8",
                title = "India Gate & Rajpath",
                location = "New Delhi",
                description = "War memorial dedicated to Indian soldiers, set in expansive lawns perfect for picnics and evening strolls.",
                category = ActivityCategory.HISTORICAL,
                rating = 4.6f,
                estimatedCost = "Free",
                duration = "1-2 hours",
                bestTime = "Evening (illuminated at night)",
                tips = listOf(
                    "Enjoy boat rides at nearby boating club",
                    "Try ice cream from local vendors",
                    "Walk to Rashtrapati Bhavan for views",
                    "Beautifully lit at night"
                )
            ),
            Activity(
                id = "9",
                title = "Lotus Temple",
                location = "New Delhi",
                description = "Architectural marvel shaped like a lotus flower, a Bahá'í House of Worship open to all religions.",
                category = ActivityCategory.CULTURE,
                rating = 4.7f,
                estimatedCost = "Free entry",
                duration = "1 hour",
                bestTime = "Morning or late afternoon",
                tips = listOf(
                    "Maintain silence inside the hall",
                    "No photography inside the temple",
                    "Beautiful gardens surrounding the temple",
                    "Meditation sessions available"
                )
            ),
            Activity(
                id = "10",
                title = "Elephanta Caves",
                location = "Mumbai (Island)",
                description = "Ancient cave temples dedicated to Lord Shiva, accessible by ferry from Gateway of India. UNESCO World Heritage Site.",
                category = ActivityCategory.HISTORICAL,
                rating = 4.4f,
                estimatedCost = "₹40 + ferry ₹200",
                duration = "Half day",
                bestTime = "Morning (9 AM - 12 PM)",
                tips = listOf(
                    "Ferry from Gateway of India (1 hour ride)",
                    "Climb 120 steps to reach caves",
                    "Closed on Mondays",
                    "Carry water and wear comfortable shoes"
                )
            )
        )
    }

    var selectedCategory by remember { mutableStateOf<ActivityCategory?>(null) }
    var expandedId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Recommended Activities",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Discover amazing places and experiences along your route",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All") },
                leadingIcon = if (selectedCategory == null) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )

            FilterChip(
                selected = selectedCategory == ActivityCategory.HISTORICAL,
                onClick = {
                    selectedCategory = if (selectedCategory == ActivityCategory.HISTORICAL) null
                    else ActivityCategory.HISTORICAL
                },
                label = { Text("Historical") }
            )

            FilterChip(
                selected = selectedCategory == ActivityCategory.FOOD,
                onClick = {
                    selectedCategory = if (selectedCategory == ActivityCategory.FOOD) null
                    else ActivityCategory.FOOD
                },
                label = { Text("Food") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == ActivityCategory.NATURE,
                onClick = {
                    selectedCategory = if (selectedCategory == ActivityCategory.NATURE) null
                    else ActivityCategory.NATURE
                },
                label = { Text("Nature") }
            )

            FilterChip(
                selected = selectedCategory == ActivityCategory.SHOPPING,
                onClick = {
                    selectedCategory = if (selectedCategory == ActivityCategory.SHOPPING) null
                    else ActivityCategory.SHOPPING
                },
                label = { Text("Shopping") }
            )

            FilterChip(
                selected = selectedCategory == ActivityCategory.CULTURE,
                onClick = {
                    selectedCategory = if (selectedCategory == ActivityCategory.CULTURE) null
                    else ActivityCategory.CULTURE
                },
                label = { Text("Culture") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Activities list
        val filteredActivities = if (selectedCategory == null) activities
        else activities.filter { it.category == selectedCategory }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredActivities) { activity ->
                ActivityCard(
                    activity = activity,
                    isExpanded = expandedId == activity.id,
                    onExpandChange = {
                        expandedId = if (expandedId == activity.id) null else activity.id
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(
    activity: Activity,
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
        )
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
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

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
                            text = activity.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Category and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Surface(
                    color = activity.category.color,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = activity.category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Text(
                            text = activity.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFB800)
                    )
                    Text(
                        text = activity.rating.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = activity.description,
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

                    // Quick info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoChip(
                            icon = Icons.Default.AttachMoney,
                            label = "Cost",
                            value = activity.estimatedCost
                        )
                        InfoChip(
                            icon = Icons.Default.Schedule,
                            label = "Duration",
                            value = activity.duration
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoChip(
                        icon = Icons.Default.WbSunny,
                        label = "Best Time",
                        value = activity.bestTime
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tips
                    Text(
                        text = "Insider Tips",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    activity.tips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFFF59E0B)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

val ActivityCategory.icon: ImageVector
    get() = when (this) {
        ActivityCategory.HISTORICAL -> Icons.Default.AccountBalance
        ActivityCategory.NATURE -> Icons.Default.Landscape
        ActivityCategory.FOOD -> Icons.Default.Restaurant
        ActivityCategory.ADVENTURE -> Icons.Default.Hiking
        ActivityCategory.SHOPPING -> Icons.Default.ShoppingBag
        ActivityCategory.CULTURE -> Icons.Default.TheaterComedy
    }

val ActivityCategory.displayName: String
    get() = when (this) {
        ActivityCategory.HISTORICAL -> "Historical"
        ActivityCategory.NATURE -> "Nature"
        ActivityCategory.FOOD -> "Food"
        ActivityCategory.ADVENTURE -> "Adventure"
        ActivityCategory.SHOPPING -> "Shopping"
        ActivityCategory.CULTURE -> "Culture"
    }

val ActivityCategory.color: Color
    get() = when (this) {
        ActivityCategory.HISTORICAL -> Color(0xFF7C3AED)
        ActivityCategory.NATURE -> Color(0xFF10B981)
        ActivityCategory.FOOD -> Color(0xFFEF4444)
        ActivityCategory.ADVENTURE -> Color(0xFFF59E0B)
        ActivityCategory.SHOPPING -> Color(0xFFEC4899)
        ActivityCategory.CULTURE -> Color(0xFF3B82F6)
    }
