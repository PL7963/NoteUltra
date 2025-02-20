package com.coolkie.noteultra.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.coolkie.noteultra.data.DarkTheme
import com.coolkie.noteultra.data.SettingsRepository
import com.coolkie.noteultra.data.dataStore

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun NoteUltraTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val repository = remember(context) { SettingsRepository(context.dataStore) }
    val darkThemeFlow = repository.darkThemeFlow.collectAsState(repository.darkThemeFirst).value
    val darkTheme = when (darkThemeFlow) {
        DarkTheme.ENABLED -> true
        DarkTheme.DISABLED -> false
        DarkTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    LaunchedEffect(darkTheme) {
        if (view.isAttachedToWindow) {
            val window = (view.context as? android.app.Activity)?.window ?: return@LaunchedEffect
            val windowInsetsController = WindowInsetsControllerCompat(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}