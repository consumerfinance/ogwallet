package dev.consumerfinance.ogwallet.ui.screens.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.db.TripRepository
import dev.consumerfinance.ogwallet.models.travel.PlanningTab
import dev.consumerfinance.ogwallet.models.travel.RoutePoint
import dev.consumerfinance.ogwallet.models.travel.Trip
import dev.consumerfinance.ogwallet.ui.screens.travel.panels.ActivitiesPanel
import dev.consumerfinance.ogwallet.ui.screens.travel.panels.CostTrackerPanel
import dev.consumerfinance.ogwallet.ui.screens.travel.panels.CreditCardRewardsPanel
import dev.consumerfinance.ogwallet.ui.screens.travel.panels.StopoverHintsPanel
import dev.consumerfinance.ogwallet.ui.screens.travel.panels.TravelChecklistPanel
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun TravelPlansScreen(
    trip: Trip,
    tripRepository: TripRepository,
    onBackToSelect: () -> Unit
) {
    var routes by remember {
        mutableStateOf(
            listOf(
                RoutePoint("1", 28.6139, 77.2090, "New Delhi"),
                RoutePoint("2", 19.0760, 72.8777, "Mumbai")
            )
        )
    }
    var activeTab by remember { mutableStateOf(PlanningTab.CHECKLIST) }
    var isPanelOpen by remember { mutableStateOf(false) }
    var showWaypoints by remember { mutableStateOf(true) }
    var editingId by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val isMobile = configuration.screenWidthDp < 768

    val onMapClick: (Position, DpOffset) -> ClickResult = { position, dpOffset ->
        val newPoint = RoutePoint(
            id = Clock.System.now().toEpochMilliseconds().toString(),
            position = position,
            label = "Point ${routes.size + 1}"
        )
        routes = routes + newPoint
        ClickResult.Pass
    }

    val removeRoute: (String) -> Unit = { id ->
        routes = routes.filter { it.id != id }
    }

    val updateRouteLabel: (String, String) -> Unit = { id, label ->
        routes = routes.map { if (it.id == id) it.copy(label = label) else it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Logo",
                            modifier = Modifier.size(if (isMobile) 24.dp else 32.dp)
                        )
                        Column {
                            Text(
                                text = trip.destination,
                                style = if (isMobile) MaterialTheme.typography.titleLarge
                                else MaterialTheme.typography.headlineSmall
                            )
                            if (!isMobile) {
                                Text(
                                    text = trip.dates,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToSelect) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to trip selection"
                        )
                    }
                },
                actions = {
                    if (isMobile) {
                        IconButton(onClick = { isPanelOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isMobile) {
            // Mobile Layout
            MobileLayout(
                modifier = Modifier.padding(paddingValues),
                routes = routes,
                showWaypoints = showWaypoints,
                editingId = editingId,
                onShowWaypointsChange = { showWaypoints = it },
                onEditingIdChange = { editingId = it },
                onMapClick = onMapClick,
                onRemoveRoute = removeRoute,
                onUpdateRouteLabel = updateRouteLabel,
                onOpenPanel = { isPanelOpen = true }
            )

            // Mobile Bottom Sheet
            if (isPanelOpen) {
                MobileBottomSheet(
                    activeTab = activeTab,
                    tripId = trip.id,
                    tripRepository = tripRepository,
                    onTabChange = { activeTab = it },
                    onDismiss = { isPanelOpen = false }
                )
            }
        } else {
            // Desktop Layout
            DesktopLayout(
                modifier = Modifier.padding(paddingValues),
                routes = routes,
                activeTab = activeTab,
                tripId = trip.id,
                tripRepository = tripRepository,
                editingId = editingId,
                onTabChange = { activeTab = it },
                onEditingIdChange = { editingId = it },
                onMapClick = onMapClick,
                onRemoveRoute = removeRoute,
                onUpdateRouteLabel = updateRouteLabel
            )
        }
    }
}

@Composable
fun MobileLayout(
    modifier: Modifier = Modifier,
    routes: List<RoutePoint>,
    showWaypoints: Boolean,
    editingId: String?,
    onShowWaypointsChange: (Boolean) -> Unit,
    onEditingIdChange: (String?) -> Unit,
    onMapClick: (Position, DpOffset) -> ClickResult,
    onRemoveRoute: (String) -> Unit,
    onUpdateRouteLabel: (String, String) -> Unit,
    onOpenPanel: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Map
        MapView(
            modifier = Modifier.fillMaxSize(),
            routes = routes,
            onMapClick = onMapClick
        )

        // Waypoints Overlay
        if (showWaypoints) {
            WaypointsOverlay(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .fillMaxWidth(0.95f),
                routes = routes,
                editingId = editingId,
                isMobile = true,
                onEditingIdChange = onEditingIdChange,
                onRemoveRoute = onRemoveRoute,
                onUpdateRouteLabel = onUpdateRouteLabel,
                onClose = { onShowWaypointsChange(false) }
            )
        }

        // Show waypoints button when hidden
        if (!showWaypoints) {
            FloatingActionButton(
                onClick = { onShowWaypointsChange(true) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Show waypoints",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Bottom Sheet Trigger Button
        FloatingActionButton(
            onClick = onOpenPanel,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Open planning tools",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun DesktopLayout(
    modifier: Modifier = Modifier,
    routes: List<RoutePoint>,
    activeTab: PlanningTab,
    tripId: String,
    tripRepository: TripRepository,
    editingId: String?,
    onTabChange: (PlanningTab) -> Unit,
    onEditingIdChange: (String?) -> Unit,
    onMapClick: (Position, DpOffset) -> ClickResult,
    onRemoveRoute: (String) -> Unit,
    onUpdateRouteLabel: (String, String) -> Unit
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Map Section
        Box(modifier = Modifier.weight(1f)) {
            Column {
                // Map Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Route Map",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Click on the map to add waypoints",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Map
                Box(modifier = Modifier.fillMaxSize()) {
                    MapView(
                        modifier = Modifier.fillMaxSize(),
                        routes = routes,
                        onMapClick = onMapClick
                    )

                    // Waypoints Overlay
                    WaypointsOverlay(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .widthIn(max = 320.dp),
                        routes = routes,
                        editingId = editingId,
                        isMobile = false,
                        onEditingIdChange = onEditingIdChange,
                        onRemoveRoute = onRemoveRoute,
                        onUpdateRouteLabel = onUpdateRouteLabel,
                        onClose = null
                    )
                }
            }
        }

        // Right Panel
        Surface(
            modifier = Modifier.width(450.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column {
                // Tabs
                TabRow(selectedTabIndex = activeTab.ordinal) {
                    PlanningTab.entries.forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick = { onTabChange(tab) },
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(tab.displayName)
                                }
                            }
                        )
                    }
                }

                // Content
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        PlanningTab.CHECKLIST -> TravelChecklistPanel(tripId = tripId, tripRepository = tripRepository)
                        PlanningTab.COSTS -> CostTrackerPanel(tripId = tripId, tripRepository = tripRepository)
                        PlanningTab.REWARDS -> CreditCardRewardsPanel()
                        PlanningTab.STOPOVERS -> StopoverHintsPanel()
                        PlanningTab.ACTIVITIES -> ActivitiesPanel()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileBottomSheet(
    activeTab: PlanningTab,
    tripId: String,
    tripRepository: TripRepository,
    onTabChange: (PlanningTab) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        // Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        ) {
            // Bottom Sheet
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) { /* Prevent closing when clicking inside */ },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    // Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Planning Tools",
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    // Tabs
                    ScrollableTabRow(
                        selectedTabIndex = activeTab.ordinal,
                        edgePadding = 8.dp
                    ) {
                        PlanningTab.entries.forEach { tab ->
                            Tab(
                                selected = activeTab == tab,
                                onClick = { onTabChange(tab) },
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = tab.displayName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Content
                    Box(modifier = Modifier.weight(1f)) {
                        when (activeTab) {
                            PlanningTab.CHECKLIST -> TravelChecklistPanel(tripId = tripId, tripRepository = tripRepository)
                            PlanningTab.COSTS -> CostTrackerPanel(tripId = tripId, tripRepository = tripRepository)
                            PlanningTab.REWARDS -> CreditCardRewardsPanel()
                            PlanningTab.STOPOVERS -> StopoverHintsPanel()
                            PlanningTab.ACTIVITIES -> ActivitiesPanel()
                        }
                    }
                }
            }
        }
    }
}

val PlanningTab.icon: ImageVector
    get() = when (this) {
        PlanningTab.CHECKLIST -> Icons.Default.CheckCircle
        PlanningTab.COSTS -> Icons.Default.AccountBalance
        PlanningTab.REWARDS -> Icons.Default.CreditCard
        PlanningTab.STOPOVERS -> Icons.Default.Flight
        PlanningTab.ACTIVITIES -> Icons.Default.Info
    }

val PlanningTab.displayName: String
    get() = when (this) {
        PlanningTab.CHECKLIST -> "Checklist"
        PlanningTab.COSTS -> "Costs"
        PlanningTab.REWARDS -> "Rewards"
        PlanningTab.STOPOVERS -> "Stopovers"
        PlanningTab.ACTIVITIES -> "Activities"
    }
