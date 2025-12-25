// commonMain/src/kotlin/dev/consumerfinance/ogwallet/App.kt

package dev.consumerfinance.ogwallet

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import dev.consumerfinance.ogwallet.db.DatabaseManager
import dev.consumerfinance.ogwallet.ui.screens.*
import dev.consumerfinance.ogwallet.ui.screens.OnboardingScreen

@Composable
fun App() {
    val dbManager = koinInject<DatabaseManager>()
    val isUnlocked by dbManager.isUnlocked.collectAsState()

    // Check if onboarding is complete by checking if vault_config exists
    // We need to check this after the vault is unlocked
    var isOnboardingComplete by remember { mutableStateOf<Boolean?>(null) }

    // Check onboarding status when vault is unlocked
    LaunchedEffect(isUnlocked) {
        if (isUnlocked && isOnboardingComplete == null) {
            isOnboardingComplete = dbManager.isOnboardingComplete()
        }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface {
            when {
                // Show lock screen first if not unlocked
                !isUnlocked -> {
                    LockScreen(dbManager)
                }
                // After unlocking, check if onboarding is needed
                isOnboardingComplete == false -> {
                    OnboardingScreen(onFinished = { isOnboardingComplete = true })
                }
                // Show main app if onboarding is complete
                isOnboardingComplete == true -> {
                    MainNavigationContainer()
                }
                // Loading state while checking onboarding status
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}