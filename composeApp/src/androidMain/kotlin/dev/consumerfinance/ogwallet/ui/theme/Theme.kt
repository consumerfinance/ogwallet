package dev.consumerfinance.ogwallet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3b82f6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF93c5fd),
    onPrimaryContainer = Color(0xFF1e3a8a),
    secondary = Color(0xFF8b5cf6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFc4b5fd),
    onSecondaryContainer = Color(0xFF5b21b6),
    tertiary = Color(0xFF10b981),
    onTertiary = Color.White,
    error = Color(0xFFef4444),
    errorContainer = Color(0xFFfecaca),
    onError = Color.White,
    onErrorContainer = Color(0xFF7f1d1d),
    background = Color(0xFFf8fafc),
    onBackground = Color(0xFF0f172a),
    surface = Color.White,
    onSurface = Color(0xFF0f172a),
    surfaceVariant = Color(0xFFf1f5f9),
    onSurfaceVariant = Color(0xFF64748b),
    outline = Color(0xFFe2e8f0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60a5fa),
    onPrimary = Color(0xFF1e3a8a),
    primaryContainer = Color(0xFF1e40af),
    onPrimaryContainer = Color(0xFFdbeafe),
    secondary = Color(0xFFa78bfa),
    onSecondary = Color(0xFF5b21b6),
    secondaryContainer = Color(0xFF6d28d9),
    onSecondaryContainer = Color(0xFFede9fe),
    tertiary = Color(0xFF34d399),
    onTertiary = Color(0xFF065f46),
    error = Color(0xFFf87171),
    errorContainer = Color(0xFF991b1b),
    onError = Color(0xFF7f1d1d),
    onErrorContainer = Color(0xFFfee2e2),
    background = Color(0xFF0f172a),
    onBackground = Color(0xFFf8fafc),
    surface = Color(0xFF1e293b),
    onSurface = Color(0xFFf8fafc),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94a3b8),
    outline = Color(0xFF475569)
)

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
