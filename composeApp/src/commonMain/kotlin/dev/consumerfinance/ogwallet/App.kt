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
import dev.consumerfinance.ogwallet.models.ThemeMode
import androidx.compose.foundation.isSystemInDarkTheme
import dev.consumerfinance.ogwallet.ui.theme.DarkColorScheme
import dev.consumerfinance.ogwallet.ui.theme.LightColorScheme
import dev.consumerfinance.ogwallet.auth.BiometricAuth

@Composable
fun App() {
    val dbManager = koinInject<DatabaseManager>()
    val biometricAuth = koinInject<BiometricAuth>()
    val isUnlocked by dbManager.isUnlocked.collectAsState()
    var justFinishedOnboarding by remember { mutableStateOf(false) }

    val currentThemeMode by dbManager.getThemeMode().collectAsState(initial = ThemeMode.SYSTEM)
    val useDarkTheme = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme) {
        Surface {
            when {
                // Just finished onboarding: go directly to main app
                justFinishedOnboarding -> {
                    MainNavigationContainer()
                }
                // First-time user: onboarding not complete, show onboarding directly
                !biometricAuth.isOnboardingComplete() -> {
                    OnboardingScreen(onFinished = {
                        justFinishedOnboarding = true
                        biometricAuth.setOnboardingComplete(true)
                    })
                }
                // Onboarding complete but vault not unlocked: show lock screen
                !isUnlocked -> {
                    LockScreen(dbManager)
                }
                // Normal operation: unlocked and onboarding complete
                else -> {
                    MainNavigationContainer()
                }
            }
        }
    }
}
