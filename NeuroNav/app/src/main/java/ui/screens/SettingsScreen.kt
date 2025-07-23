package com.finesi.neuronav.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finesi.neuronav.ui.theme.*
import com.finesi.neuronav.utils.PermissionHelper
import androidx.compose.ui.graphics.Color

// Modelo para elementos de configuración
data class SettingItemData(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val type: SettingType,
    val isEnabled: Boolean = true,
    val hasSwitch: Boolean = false,
    val switchState: Boolean = false,
    val category: String = "General"
)

enum class SettingType {
    ACTION, SWITCH, INFO, NAVIGATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(generateSettingsItems()) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface.copy(alpha = 0.8f),
                        DarkBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCard
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onBackClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = NeuroBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "CONFIGURACIÓN",
                            color = NeuroBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sistema NeuroNav v1.0",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Lista de configuraciones por categorías
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = settings.groupBy { it.category }

                categories.forEach { (category, categorySettings) ->
                    item {
                        CategoryHeader(category = category)
                    }

                    items(categorySettings) { setting ->
                        SettingItemCard(
                            setting = setting,
                            onToggle = { newState ->
                                settings = settings.map {
                                    if (it.id == setting.id) {
                                        it.copy(switchState = newState)
                                    } else {
                                        it
                                    }
                                }
                                handleSettingToggle(context, setting.id, newState)
                            },
                            onClick = {
                                handleSettingClick(context, setting,
                                    onShowAbout = { showAboutDialog = true },
                                    onShowTheme = { showThemeDialog = true }
                                )
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Diálogo "Acerca de"
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    // Diálogo de tema
    if (showThemeDialog) {
        ThemeDialog(onDismiss = { showThemeDialog = false })
    }
}

@Composable
fun CategoryHeader(category: String) {
    Text(
        text = category.uppercase(),
        color = TextAccent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun SettingItemCard(
    setting: SettingItemData,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = setting.isEnabled) {
                if (setting.type != SettingType.SWITCH) {
                    onClick()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getSettingIconColor(setting.id).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = setting.icon,
                    contentDescription = null,
                    tint = getSettingIconColor(setting.id),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = setting.title,
                    color = if (setting.isEnabled) TextPrimary else TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                if (setting.subtitle.isNotEmpty()) {
                    Text(
                        text = setting.subtitle,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Switch o indicador
            when (setting.type) {
                SettingType.SWITCH -> {
                    Switch(
                        checked = setting.switchState,
                        onCheckedChange = onToggle,
                        enabled = setting.isEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = getSettingIconColor(setting.id),
                            checkedTrackColor = getSettingIconColor(setting.id).copy(alpha = 0.3f)
                        )
                    )
                }
                SettingType.NAVIGATION -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                SettingType.INFO -> {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                SettingType.ACTION -> {
                    // Sin indicador adicional
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = NeuroBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("NeuroNav", color = TextPrimary)
            }
        },
        text = {
            Column {
                Text("Versión: 1.0.0", color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sistema de Navegación Neural Inteligente", color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Desarrollado con Jetpack Compose", color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("© 2025 NeuroNav. Todos los derechos reservados.", color = TextSecondary, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = NeuroBlue)
            }
        },
        containerColor = DarkCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
fun ThemeDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Seleccionar Tema", color = TextPrimary)
        },
        text = {
            Column {
                ThemeOption(
                    title = "Tema Oscuro",
                    description = "Interfaz oscura con colores neón",
                    isSelected = ThemeManager.currentTheme == ThemeType.DARK,
                    onClick = {
                        ThemeManager.setTheme(context, ThemeType.DARK)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ThemeOption(
                    title = "Tema Claro",
                    description = "Interfaz clara y minimalista",
                    isSelected = ThemeManager.currentTheme == ThemeType.LIGHT,
                    onClick = {
                        ThemeManager.setTheme(context, ThemeType.LIGHT)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = NeuroBlue)
            }
        },
        containerColor = if (ThemeManager.currentTheme == ThemeType.DARK) DarkCard else Color(0xFFF0F0F0),
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
fun ThemeOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NeuroBlue.copy(alpha = 0.2f)
            else if (ThemeManager.currentTheme == ThemeType.DARK) DarkSurface else Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = NeuroBlue,
                    unselectedColor = TextSecondary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun getSettingIconColor(settingId: String): androidx.compose.ui.graphics.Color {
    return when (settingId) {
        "theme" -> NeuroPurple
        "permissions" -> NeuroOrange
        "storage" -> NeuroGreen
        "performance" -> NeuroBlue
        "gestures" -> NeuroPurple
        "notifications" -> NeuroOrange
        "backup" -> NeuroGreen
        "security" -> NeuroBlue
        "about" -> TextSecondary
        "help" -> NeuroGreen
        "privacy" -> NeuroOrange
        "animations" -> NeuroPurple
        "quick_access" -> NeuroBlue
        "auto_sync" -> NeuroGreen
        "storage_permission" -> NeuroOrange
        "cache_management" -> NeuroBlue
        "background_service" -> NeuroPurple
        "usage_stats" -> NeuroOrange
        "crash_reports" -> NeuroGreen
        "feedback" -> NeuroBlue
        else -> TextSecondary
    }
}

fun handleSettingClick(
    context: Context,
    setting: SettingItemData,
    onShowAbout: () -> Unit,
    onShowTheme: () -> Unit
) {
    when (setting.id) {
        "permissions" -> {
            PermissionHelper.openAppSettings(context)
        }
        "storage_permission" -> {
            PermissionHelper.requestAllFilesAccess(context)
        }
        "about" -> {
            onShowAbout()
        }
        "theme" -> {
            onShowTheme()
        }
        "help" -> {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NeuroNav/help"))
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback - abrir configuración de la app
                PermissionHelper.openAppSettings(context)
            }
        }
        "feedback" -> {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback@neuronav.app"))
                    putExtra(Intent.EXTRA_SUBJECT, "NeuroNav - Comentarios")
                    putExtra(Intent.EXTRA_TEXT, "Hola equipo NeuroNav,\n\nMi comentario es:\n\n")
                }
                context.startActivity(Intent.createChooser(intent, "Enviar comentarios"))
            } catch (e: Exception) {
                // Si no hay app de email, mostrar configuración
                PermissionHelper.openAppSettings(context)
            }
        }
        "cache_management" -> {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            } catch (e: Exception) {
                PermissionHelper.openAppSettings(context)
            }
        }
    }
}

fun handleSettingToggle(context: Context, settingId: String, isEnabled: Boolean) {
    // Aquí puedes implementar la lógica para cada toggle
    when (settingId) {
        "animations" -> {
            // Guardar preferencia de animaciones
        }
        "gestures" -> {
            // Guardar preferencia de gestos
        }
        "notifications" -> {
            if (isEnabled) {
                // Solicitar permisos de notificación
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Lógica para Android 13+
                }
            }
        }
        // Otros toggles...
    }
}

fun generateSettingsItems(): List<SettingItemData> {
    return listOf(
        // Categoría Apariencia
        SettingItemData(
            id = "theme",
            title = "Tema Neural",
            subtitle = "Personaliza la apariencia de NeuroNav",
            icon = Icons.Default.Palette,
            type = SettingType.NAVIGATION,
            category = "Apariencia"
        ),
        SettingItemData(
            id = "animations",
            title = "Animaciones",
            subtitle = "Efectos visuales y transiciones",
            icon = Icons.Default.Animation,
            type = SettingType.SWITCH,
            hasSwitch = true,
            switchState = true,
            category = "Apariencia"
        ),

        // Categoría Funcionalidad
        SettingItemData(
            id = "gestures",
            title = "Gestos",
            subtitle = "Navegación por gestos táctiles",
            icon = Icons.Default.Gesture,
            type = SettingType.SWITCH,
            hasSwitch = true,
            switchState = true,
            category = "Funcionalidad"
        ),
        SettingItemData(
            id = "quick_access",
            title = "Acceso Rápido",
            subtitle = "Botón flotante de navegación",
            icon = Icons.Default.Speed,
            type = SettingType.SWITCH,
            hasSwitch = true,
            switchState = true,
            category = "Funcionalidad"
        ),

        // Categoría Permisos
        SettingItemData(
            id = "permissions",
            title = "Permisos de la App",
            subtitle = "Gestionar permisos del sistema",
            icon = Icons.Default.Security,
            type = SettingType.ACTION,
            category = "Permisos"
        ),
        SettingItemData(
            id = "storage_permission",
            title = "Acceso a Archivos",
            subtitle = "Permitir acceso completo al almacenamiento",
            icon = Icons.Default.Storage,
            type = SettingType.ACTION,
            category = "Permisos"
        ),

        // Categoría Sistema
        SettingItemData(
            id = "cache_management",
            title = "Gestión de Caché",
            subtitle = "Optimizar velocidad de carga",
            icon = Icons.Default.Memory,
            type = SettingType.ACTION,
            category = "Sistema"
        ),
        SettingItemData(
            id = "background_service",
            title = "Servicio en Segundo Plano",
            subtitle = "Mantener NeuroNav activo",
            icon = Icons.Default.CloudDone,
            type = SettingType.SWITCH,
            hasSwitch = true,
            switchState = false,
            category = "Sistema"
        ),

        // Categoría Información
        SettingItemData(
            id = "about",
            title = "Acerca de NeuroNav",
            subtitle = "Versión 1.0 - Neural Navigation System",
            icon = Icons.Default.Info,
            type = SettingType.NAVIGATION,
            category = "Información"
        ),
        SettingItemData(
            id = "help",
            title = "Ayuda y Soporte",
            subtitle = "Guías y documentación",
            icon = Icons.Default.Help,
            type = SettingType.ACTION,
            category = "Información"
        ),
        SettingItemData(
            id = "feedback",
            title = "Enviar Comentarios",
            subtitle = "Comparte tu experiencia",
            icon = Icons.Default.Feedback,
            type = SettingType.ACTION,
            category = "Información"
        )
    )
}