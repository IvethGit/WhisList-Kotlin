package com.iviapps.whislistbyivi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

val LightPrimary = Color(0xFFFF00A1)
val LightSecondary = Color(0xFFFF00A1)
val LightTertiary = Color(0xFF7D5260)

val DarkPrimary = Color(0xFFFF00A1)
val DarkSecondary = Color(0xFFFF479C)
val DarkTertiary = Color(0xFF2A2627)

val LightError = Color(0xFF90416A)
val DarkError = Color(0xFFB34A9E)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFF00A1),
    onPrimaryContainer = Color.White,

    secondary = DarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBF74A6),
    onSecondaryContainer = Color.White,

    tertiary = DarkTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3C2B2D),
    onTertiaryContainer = Color.White,
//fondo de admin
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFF00A1),

    error = DarkError,
    onError = Color.Black,


)
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFF00A1),
    onPrimaryContainer = Color.White,

    secondary = LightSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFBF74A6),
    onSecondaryContainer = Color.White,

    tertiary = LightTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFF729F),
    onTertiaryContainer = Color(0xFFFF7C99),

    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFEE3CCE),
    onSurface = Color(0xFF1C1B1F),

    error = LightError,
    onError = Color.White,
)

@Composable
fun WhisListByIviTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
