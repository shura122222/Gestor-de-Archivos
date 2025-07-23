package com.finesi.neuronav.ui.theme

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

// Colores personalizados para NeuroNav
private val NeuroNavPrimary = Color(0xFF00E676)
private val NeuroNavSecondary = Color(0xFF1A1A2E)
private val NeuroNavTertiary = Color(0xFF16213E)
private val NeuroNavSurface = Color(0xFF0F0F23)
private val NeuroNavBackground = Color(0xFF000000)

private val DarkColorScheme = darkColorScheme(
    primary = NeuroNavPrimary,
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3),
    background = NeuroNavBackground,
    surface = NeuroNavSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = NeuroNavSecondary,
    onSurfaceVariant = Color.White.copy(alpha = 0.8f),
    outline = Color.White.copy(alpha = 0.3f),
    inverseOnSurface = Color.Black,
    inverseSurface = Color.White,
    inversePrimary = Color(0xFF006B3D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006B3D),
    secondary = Color(0xFF526350),
    tertiary = Color(0xFF39656A),
    background = Color(0xFFFCFDF7),
    surface = Color(0xFFFCFDF7),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1C18),
    onSurface = Color(0xFF1A1C18),
    surfaceVariant = Color(0xFFDEE5D9),
    onSurfaceVariant = Color(0xFF424940),
    outline = Color(0xFF727970),
    inverseOnSurface = Color(0xFFF1F1EB),
    inverseSurface = Color(0xFFF1F1EB),
)