package dev.consumerfinance.ogwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import dev.consumerfinance.ogwallet.ui.theme.WalletTheme
import dev.consumerfinance.ogwallet.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinanceHubTheme {
                FinanceHubApp()
            }
        }
    }
}

@Composable
fun FinanceHubApp() {
    var selectedTab by remember { mutableStateOf(0) }
    
    val navigationItems = listOf(
        NavigationItem("Home", Icons.Filled.Home),
        NavigationItem("Cards", Icons.Filled.CreditCard),
        NavigationItem("Wallet", Icons.Filled.AccountBalanceWallet),
        NavigationItem("Offers", Icons.Filled.CardGiftcard),
        NavigationItem("Travel", Icons.Filled.Flight),
        NavigationItem("Stats", Icons.Filled.TrendingUp)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                "FinanceHub",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "JD",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(onNavigate = { selectedTab = it })
                1 -> CreditCardsScreen()
                2 -> WalletScreen()
                3 -> OffersRewardsScreen()
                4 -> TravelPlansScreen()
                5 -> BudgetAnalyticsScreen()
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector
)
}