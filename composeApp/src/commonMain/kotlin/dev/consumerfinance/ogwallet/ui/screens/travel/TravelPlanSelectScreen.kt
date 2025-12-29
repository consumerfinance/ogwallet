package dev.consumerfinance.ogwallet.ui.screens.travel

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.db.TripRepository
import dev.consumerfinance.ogwallet.models.travel.Trip
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlanSelectScreen(
    onTripSelected: (Trip) -> Unit
) {
    val tripRepository: TripRepository = koinInject()
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val trips by tripRepository.getAllTrips().collectAsState(initial = emptyList())

    val configuration = LocalConfiguration.current
    val isMobile = configuration.screenWidthDp < 768

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TravelExplore,
                            contentDescription = "Travel",
                            modifier = Modifier.size(if (isMobile) 24.dp else 32.dp)
                        )
                        Column {
                            Text(
                                text = "Travel Plans",
                                style = if (isMobile) MaterialTheme.typography.titleLarge
                                else MaterialTheme.typography.headlineSmall
                            )
                            if (!isMobile) {
                                Text(
                                    text = "Select or create a new trip",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new trip"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(trips) { trip ->
                TripCard(
                    trip = trip,
                    onClick = { onTripSelected(trip) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddTripDialog(
            onDismiss = { showAddDialog = false },
            onTripCreated = { newTrip ->
                scope.launch {
                    tripRepository.addTrip(newTrip)
                    onTripSelected(newTrip)
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(
                text = trip.emoji,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Trip details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = trip.dates,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = trip.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (trip.status) {
                            "Planned" -> Color(0xFF4CAF50)
                            "Booked" -> Color(0xFF2196F3)
                            "Draft" -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (trip.bookings > 0) {
                        Text(
                            text = "${trip.bookings} bookings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Savings/Points
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (trip.savings > 0) {
                    Text(
                        text = "₹${trip.savings} saved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
                if (trip.pointsUsed > 0) {
                    Text(
                        text = "${trip.pointsUsed} pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripDialog(
    onDismiss: () -> Unit,
    onTripCreated: (Trip) -> Unit
) {
    var destination by remember { mutableStateOf("") }
    var dates by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Trip")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination") },
                    placeholder = { Text("e.g., Tokyo, Japan") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dates,
                    onValueChange = { dates = it },
                    label = { Text("Dates") },
                    placeholder = { Text("e.g., Dec 15-22, 2024") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (destination.isNotBlank() && dates.isNotBlank()) {
                        val newTrip = Trip(
                            id = Clock.System.currentTimeMillis().toString(),
                            destination = destination,
                            dates = dates,
                            status = "Draft",
                            pointsUsed = 0,
                            savings = 0,
                            emoji = "✈️",
                            bookings = 0
                        )
                        onTripCreated(newTrip)
                    }
                },
                enabled = destination.isNotBlank() && dates.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}