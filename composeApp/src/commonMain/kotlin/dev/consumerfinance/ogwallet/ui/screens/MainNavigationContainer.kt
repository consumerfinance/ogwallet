package dev.consumerfinance.ogwallet.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import dev.consumerfinance.ogwallet.getPlatform
import dev.consumerfinance.ogwallet.ui.screens.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainNavigationContainer() {
    var selectedTab by remember { mutableStateOf(0) }

    val navigationItems = listOf(
        NavigationItem("Home", Icons.Filled.Home, "home"),
        NavigationItem("Cards", Icons.Filled.CreditCard, "cards"),
        NavigationItem("Wallet", Icons.Filled.AccountBalanceWallet, "wallet"),
        NavigationItem("Offers", Icons.Filled.CardGiftcard, "offers"),
        NavigationItem("Travel", Icons.Filled.Flight, "travel"),
        NavigationItem("Stats", Icons.Filled.TrendingUp, "stats"),
        NavigationItem("Settings", Icons.Filled.Settings, "settings")
    )

    // Detect platform for responsive layout
    val platform = getPlatform()
    val isDesktopOrWeb = platform.name.contains("Java") ||
                         platform.name.contains("Web") ||
                         platform.name.contains("Wasm")

    if (isDesktopOrWeb) {
        // Desktop/Web layout with side navigation
        DesktopNavigationLayout(
            selectedTab = selectedTab,
            navigationItems = navigationItems,
            onTabSelected = { selectedTab = it }
        )
    } else {
        // Mobile layout with bottom navigation
        MobileNavigationLayout(
            selectedTab = selectedTab,
            navigationItems = navigationItems,
            onTabSelected = { selectedTab = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopNavigationLayout(
    selectedTab: Int,
    navigationItems: List<NavigationItem>,
    onTabSelected: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Main content area (left side)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(onNavigate = { tabIndex -> onTabSelected(tabIndex) })
                1 -> CreditCardsScreen()
                2 -> WalletScreen()
                3 -> OffersRewardsScreen()
                4 -> TravelPlansScreen()
                5 -> BudgetAnalyticsScreen()
                6 -> SettingsScreen()
            }
        }

        // Right side navigation drawer
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Column(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                "OGWallet",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Finance Hub",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Navigation items
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    navigationItems.forEachIndexed { index, item ->
                        NavigationDrawerItem(
                            selected = selectedTab == index,
                            onClick = { onTabSelected(index) },
                            icon = item.icon,
                            label = item.label
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Desktop Mode",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "v1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationDrawerItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileNavigationLayout(
    selectedTab: Int,
    navigationItems: List<NavigationItem>,
    onTabSelected: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("FinanceHub", style = MaterialTheme.typography.titleMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            CustomBottomNavigation(
                selectedTab = selectedTab,
                items = navigationItems,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(onNavigate = { tabIndex -> onTabSelected(tabIndex) })
                1 -> CreditCardsScreen()
                2 -> WalletScreen()
                3 -> OffersRewardsScreen()
                4 -> TravelPlansScreen()
                5 -> BudgetAnalyticsScreen()
                6 -> SettingsScreen()
            }
        }
    }
}



@Composable
fun CustomBottomNavigation(
    selectedTab: Int,
    items: List<NavigationItem>,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .selectableGroup(),  // Makes the row items clickable
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEachIndexed { index, item ->
            BottomNavItem(
                icon = item.icon,
                label = item.label,
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

