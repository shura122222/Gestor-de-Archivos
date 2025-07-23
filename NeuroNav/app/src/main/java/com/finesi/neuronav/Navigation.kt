package com.finesi.neuronav

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finesi.neuronav.ui.screens.*
import com.finesi.neuronav.ui.components.SystemInfoCard
import kotlinx.coroutines.delay
import kotlin.math.*

// Modelo de datos para los atajos de accesibilidad
data class AccessibilityShortcut(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color,
    val description: String
)

// Estado global de la aplicación
class NeuroNavState {
    var isQuickAccessVisible by mutableStateOf(false)
    var isGestureMode by mutableStateOf(true)
    var selectedShortcut by mutableStateOf<AccessibilityShortcut?>(null)
    var backgroundTasksEnabled by mutableStateOf(true)

    // Atajos predefinidos
    val shortcuts = listOf(
        AccessibilityShortcut("files", "Archivos", Icons.Default.Folder, "files", Color(0xFF4CAF50), "Explorador de archivos"),
        AccessibilityShortcut("apps", "Apps", Icons.Default.Apps, "apps", Color(0xFF2196F3), "Administrador de aplicaciones"),
        AccessibilityShortcut("search", "Buscar", Icons.Default.Search, "search", Color(0xFF9C27B0), "Búsqueda universal"),
        AccessibilityShortcut("recent", "Recientes", Icons.Default.History, "recent", Color(0xFFFF9800), "Archivos recientes"),
        AccessibilityShortcut("favorites", "Favoritos", Icons.Default.Star, "favorites", Color(0xFFF44336), "Elementos favoritos"),
        AccessibilityShortcut("settings", "Ajustes", Icons.Default.Settings, "settings", Color(0xFF607D8B), "Configuración")
    )
}

@Composable
fun NeuroNavigation() {
    val navController = rememberNavController()
    val neuroState = remember { NeuroNavState() }
    val hapticFeedback = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con gradiente dinámico
        AnimatedBackground(neuroState.selectedShortcut?.color ?: Color(0xFF1A1A2E))

        // Navegación principal
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable("home") {
                EnhancedHomeScreen(
                    neuroState = neuroState,
                    onNavigate = { route ->
                        if (neuroState.backgroundTasksEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        navController.navigate(route)
                    }
                )
            }

            composable("files") {
                FileExplorerScreen(
                    onBackClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navController.popBackStack()
                    }
                )
            }

            composable("apps") {
                AppsScreen(
                    onBackClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navController.popBackStack()
                    }
                )
            }

            composable("search") {
                SearchScreen(
                    onBackClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navController.popBackStack()
                    }
                )
            }

            composable("recent") {
                RecentScreen(
                    onBackClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navController.popBackStack()
                    }
                )
            }

            composable("favorites") {
                FavoritesScreen(
                    onBackClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navController.popBackStack()
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBackClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navController.popBackStack()
                    }
                )
            }
        }

        // Overlay de acceso rápido
        AnimatedVisibility(
            visible = neuroState.isQuickAccessVisible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            QuickAccessOverlay(
                neuroState = neuroState,
                onShortcutSelected = { shortcut ->
                    neuroState.selectedShortcut = shortcut
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate(shortcut.route)
                    neuroState.isQuickAccessVisible = false
                },
                onDismiss = {
                    neuroState.isQuickAccessVisible = false
                }
            )
        }

        // Botón flotante de acceso rápido
        FloatingAccessibilityButton(
            neuroState = neuroState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun AnimatedBackground(targetColor: Color) {
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(1000),
        label = "background_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = 0.3f),
                        Color(0xFF0F0F23),
                        Color.Black
                    ),
                    radius = 1000f
                )
            )
    )
}

@Composable
fun EnhancedHomeScreen(
    neuroState: NeuroNavState,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "NeuroNav",
            fontSize = 32.sp,
            color = Color.White,
            style = MaterialTheme.typography.displayMedium
        )

        Text(
            text = "Navegación inteligente",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Información del sistema unificada
        SystemInfoCard()

        Spacer(modifier = Modifier.height(24.dp))

        // Grid de atajos principales
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(neuroState.shortcuts) { shortcut ->
                ShortcutCard(
                    shortcut = shortcut,
                    onClick = { onNavigate(shortcut.route) }
                )
            }
        }
    }
}

@Composable
fun ShortcutCard(
    shortcut: AccessibilityShortcut,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .scale(scale)
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = shortcut.color.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = shortcut.icon,
                contentDescription = shortcut.description,
                tint = shortcut.color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = shortcut.title,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickAccessOverlay(
    neuroState: NeuroNavState,
    onShortcutSelected: (AccessibilityShortcut) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() }
    ) {
        // Círculo central con atajos
        val radius = 120.dp

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(radius * 2)
        ) {
            neuroState.shortcuts.forEachIndexed { index, shortcut ->
                val angle = (index * 60f - 90f) * (PI / 180f).toFloat()
                val x = cos(angle) * radius.value
                val y = sin(angle) * radius.value

                FloatingActionButton(
                    onClick = { onShortcutSelected(shortcut) },
                    modifier = Modifier
                        .offset(
                            x = x.dp + radius - 28.dp,
                            y = y.dp + radius - 28.dp
                        )
                        .size(56.dp),
                    containerColor = shortcut.color,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = shortcut.icon,
                        contentDescription = shortcut.description
                    )
                }
            }
        }

        Text(
            text = "Selecciona una opción",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            fontSize = 16.sp
        )
    }
}

@Composable
fun FloatingAccessibilityButton(
    neuroState: NeuroNavState,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    FloatingActionButton(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            neuroState.isQuickAccessVisible = !neuroState.isQuickAccessVisible
        },
        modifier = modifier,
        containerColor = Color(0xFF00E676),
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Accessibility,
            contentDescription = "Acceso rápido"
        )
    }
}