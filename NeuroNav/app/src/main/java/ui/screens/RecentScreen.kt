package com.finesi.neuronav.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.finesi.neuronav.ui.theme.*
import com.finesi.neuronav.utils.AppManager
import com.finesi.neuronav.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos para elementos recientes
data class RecentItem(
    val id: String,
    val name: String,
    val type: RecentType,
    val timestamp: Long,
    val path: String? = null,
    val packageName: String? = null,
    val size: Long = 0L
)

enum class RecentType {
    APP, FILE, FOLDER
}

@Composable
fun RecentScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var recentItems by remember { mutableStateOf<List<RecentItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filterType by remember { mutableStateOf("all") } // "all", "apps", "files", "folders"

    // Cargar elementos recientes
    LaunchedEffect(Unit) {
        isLoading = true
        recentItems = generateRecentItems(context)
        isLoading = false
    }

    // Filtrar elementos según el tipo seleccionado
    val filteredItems = remember(recentItems, filterType) {
        when (filterType) {
            "apps" -> recentItems.filter { it.type == RecentType.APP }
            "files" -> recentItems.filter { it.type == RecentType.FILE }
            "folders" -> recentItems.filter { it.type == RecentType.FOLDER }
            else -> recentItems
        }
    }

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
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onBackClick() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Atrás",
                                tint = NeuroOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "RECIENTES",
                                color = NeuroOrange,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredItems.size} elementos encontrados",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Botón limpiar historial
                        IconButton(
                            onClick = {
                                recentItems = emptyList()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpiar historial",
                                tint = NeuroPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtros
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterType == "all",
                            onClick = { filterType = "all" },
                            label = { Text("Todo") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuroOrange.copy(alpha = 0.3f),
                                selectedLabelColor = NeuroOrange
                            )
                        )
                        FilterChip(
                            selected = filterType == "apps",
                            onClick = { filterType = "apps" },
                            label = { Text("Apps") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuroBlue.copy(alpha = 0.3f),
                                selectedLabelColor = NeuroBlue
                            )
                        )
                        FilterChip(
                            selected = filterType == "files",
                            onClick = { filterType = "files" },
                            label = { Text("Archivos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuroGreen.copy(alpha = 0.3f),
                                selectedLabelColor = NeuroGreen
                            )
                        )
                        FilterChip(
                            selected = filterType == "folders",
                            onClick = { filterType = "folders" },
                            label = { Text("Carpetas") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuroPurple.copy(alpha = 0.3f),
                                selectedLabelColor = NeuroPurple
                            )
                        )
                    }
                }
            }

            // Lista de elementos recientes
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NeuroOrange
                    )
                }
            } else if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sin elementos recientes",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { item ->
                        RecentItemCard(
                            item = item,
                            onClick = {
                                when (item.type) {
                                    RecentType.APP -> {
                                        item.packageName?.let { packageName ->
                                            AppManager.launchApp(context, packageName)
                                        }
                                    }
                                    RecentType.FILE, RecentType.FOLDER -> {
                                        item.path?.let { path ->
                                            val file = File(path)
                                            if (file.exists()) {
                                                FileUtils.openFile(context, file)
                                            }
                                        }
                                    }
                                }
                            },
                            onRemove = {
                                recentItems = recentItems.filter { it.id != item.id }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentItemCard(
    item: RecentItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = when (item.type) {
                    RecentType.APP -> NeuroBlue.copy(alpha = 0.3f)
                    RecentType.FILE -> NeuroGreen.copy(alpha = 0.3f)
                    RecentType.FOLDER -> NeuroPurple.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono específico del tipo
            when (item.type) {
                RecentType.APP -> {
                    // Intentar cargar icono real de la app
                    item.packageName?.let { packageName ->
                        val appInfo = AppManager.getAppInfo(context, packageName)
                        if (appInfo != null) {
                            val bitmap = appInfo.icon.toBitmap(48, 48)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            DefaultIcon(Icons.Default.Apps, NeuroBlue)
                        }
                    } ?: DefaultIcon(Icons.Default.Apps, NeuroBlue)
                }
                RecentType.FOLDER -> {
                    DefaultIcon(Icons.Default.Folder, NeuroPurple)
                }
                RecentType.FILE -> {
                    // Icono basado en la extensión del archivo
                    val extension = item.name.substringAfterLast(".", "")
                    val (icon, color) = when (extension.lowercase()) {
                        "jpg", "jpeg", "png", "gif" -> Icons.Default.Image to NeuroGreen
                        "mp4", "avi", "mkv" -> Icons.Default.VideoFile to NeuroBlue
                        "mp3", "wav", "flac" -> Icons.Default.AudioFile to NeuroOrange
                        "pdf" -> Icons.Default.PictureAsPdf to NeuroPurple
                        "txt", "doc", "docx" -> Icons.Default.Description to NeuroGreen
                        else -> Icons.Default.InsertDriveFile to TextSecondary
                    }
                    DefaultIcon(icon, color)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del elemento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Text(
                    text = buildString {
                        append(item.type.name.lowercase().replaceFirstChar { it.uppercase() })
                        if (item.type == RecentType.FILE && item.size > 0) {
                            append(" • ${FileUtils.formatFileSize(item.size)}")
                        }
                        append(" • ${formatTimeAgo(item.timestamp)}")
                    },
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Mostrar ruta si es archivo o carpeta
                if (item.path != null && item.type != RecentType.APP) {
                    Text(
                        text = item.path,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            // Botón eliminar
            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DefaultIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

// Función para generar elementos recientes de ejemplo
fun generateRecentItems(context: android.content.Context): List<RecentItem> {
    val items = mutableListOf<RecentItem>()
    val now = System.currentTimeMillis()

    // Apps recientes
    val apps = AppManager.getInstalledApps(context).take(3)
    apps.forEachIndexed { index, app ->
        items.add(
            RecentItem(
                id = "app_${app.packageName}",
                name = app.name,
                type = RecentType.APP,
                timestamp = now - (index * 300000L), // 5 minutos entre cada una
                packageName = app.packageName
            )
        )
    }

    // Archivos recientes (simulados)
    val recentFiles = listOf(
        "foto_vacaciones.jpg" to 1024000L,
        "documento_importante.pdf" to 512000L,
        "musica_favorita.mp3" to 3072000L,
        "video_familia.mp4" to 10240000L,
        "notas.txt" to 2048L
    )

    recentFiles.forEachIndexed { index, (fileName, size) ->
        items.add(
            RecentItem(
                id = "file_$fileName",
                name = fileName,
                type = RecentType.FILE,
                timestamp = now - ((index + 3) * 600000L), // 10 minutos entre cada uno
                path = "/storage/emulated/0/Download/$fileName",
                size = size
            )
        )
    }

    // Carpetas recientes
    val recentFolders = listOf(
        "Descargas",
        "Documentos",
        "Fotos",
        "Música"
    )

    recentFolders.forEachIndexed { index, folderName ->
        items.add(
            RecentItem(
                id = "folder_$folderName",
                name = folderName,
                type = RecentType.FOLDER,
                timestamp = now - ((index + 8) * 900000L), // 15 minutos entre cada una
                path = "/storage/emulated/0/$folderName"
            )
        )
    }

    return items.sortedByDescending { it.timestamp }
}

fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}