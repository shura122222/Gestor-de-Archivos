package com.finesi.neuronav.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

// Modelo para elementos favoritos
data class FavoriteItem(
    val id: String,
    val name: String,
    val type: FavoriteType,
    val path: String? = null,
    val packageName: String? = null,
    val addedTime: Long = System.currentTimeMillis(),
    val category: String = "General"
)

enum class FavoriteType {
    APP, FILE, FOLDER, SHORTCUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var favoriteItems by remember { mutableStateOf<List<FavoriteItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Todo") }

    // Cargar favoritos
    LaunchedEffect(Unit) {
        isLoading = true
        favoriteItems = generateFavoriteItems(context)
        isLoading = false
    }

    // Categorías disponibles
    val categories = remember(favoriteItems) {
        listOf("Todo") + favoriteItems.map { it.category }.distinct().sorted()
    }

    // Filtrar por categoría
    val filteredItems = remember(favoriteItems, selectedCategory) {
        if (selectedCategory == "Todo") {
            favoriteItems
        } else {
            favoriteItems.filter { it.category == selectedCategory }
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
                                tint = NeuroPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "FAVORITOS",
                                color = NeuroPurple,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredItems.size} elementos guardados",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Botón para agregar favorito
                        IconButton(
                            onClick = {
                                // TODO: Implementar diálogo para agregar favorito
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar favorito",
                                tint = NeuroGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtros por categoría
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories.size) { index ->
                            val category = categories[index]
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeuroPurple.copy(alpha = 0.3f),
                                    selectedLabelColor = NeuroPurple
                                )
                            )
                        }
                    }
                }
            }

            // Lista de favoritos
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NeuroPurple
                    )
                }
            } else if (filteredItems.isEmpty()) {
                EmptyFavoritesState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { item ->
                        FavoriteItemCard(
                            item = item,
                            onClick = {
                                when (item.type) {
                                    FavoriteType.APP -> {
                                        item.packageName?.let { packageName ->
                                            AppManager.launchApp(context, packageName)
                                        }
                                    }
                                    FavoriteType.FILE, FavoriteType.FOLDER -> {
                                        item.path?.let { path ->
                                            val file = File(path)
                                            if (file.exists()) {
                                                FileUtils.openFile(context, file)
                                            }
                                        }
                                    }
                                    FavoriteType.SHORTCUT -> {
                                        // TODO: Manejar shortcuts personalizados
                                    }
                                }
                            },
                            onRemove = {
                                favoriteItems = favoriteItems.filter { it.id != item.id }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItemCard(
    item: FavoriteItem,
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
                color = NeuroPurple.copy(alpha = 0.3f),
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
            // Icono de favorito
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = NeuroPurple,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Icono específico del tipo
            when (item.type) {
                FavoriteType.APP -> {
                    item.packageName?.let { packageName ->
                        val appInfo = AppManager.getAppInfo(context, packageName)
                        if (appInfo != null) {
                            val bitmap = appInfo.icon.toBitmap(40, 40)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            DefaultFavoriteIcon(Icons.Default.Apps, NeuroBlue)
                        }
                    } ?: DefaultFavoriteIcon(Icons.Default.Apps, NeuroBlue)
                }
                FavoriteType.FOLDER -> {
                    DefaultFavoriteIcon(Icons.Default.Folder, NeuroGreen)
                }
                FavoriteType.FILE -> {
                    DefaultFavoriteIcon(Icons.Default.InsertDriveFile, NeuroOrange)
                }
                FavoriteType.SHORTCUT -> {
                    DefaultFavoriteIcon(Icons.Default.Shortcut, NeuroPurple)
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    if (item.category != "General") {
                        Text(
                            text = " • ${item.category}",
                            color = NeuroPurple.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Mostrar ruta si aplica
                if (item.path != null && item.type != FavoriteType.APP) {
                    Text(
                        text = item.path,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            // Botón eliminar de favoritos
            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    imageVector = Icons.Default.StarBorder,
                    contentDescription = "Quitar de favoritos",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DefaultFavoriteIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EmptyFavoritesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin favoritos",
                color = TextSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Agrega elementos que uses frecuentemente",
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

// Función para generar favoritos de ejemplo
fun generateFavoriteItems(context: android.content.Context): List<FavoriteItem> {
    val items = mutableListOf<FavoriteItem>()

    // Apps favoritas
    val popularApps = listOf("com.whatsapp", "com.android.chrome", "com.spotify.music")
    popularApps.forEach { packageName ->
        val appInfo = AppManager.getAppInfo(context, packageName)
        if (appInfo != null) {
            items.add(
                FavoriteItem(
                    id = "fav_app_$packageName",
                    name = appInfo.name,
                    type = FavoriteType.APP,
                    packageName = packageName,
                    category = "Apps"
                )
            )
        }
    }

    // Carpetas favoritas
    val favoriteFolders = listOf(
        "Descargas" to "/storage/emulated/0/Download",
        "Fotos" to "/storage/emulated/0/Pictures",
        "Documentos" to "/storage/emulated/0/Documents"
    )

    favoriteFolders.forEach { (name, path) ->
        items.add(
            FavoriteItem(
                id = "fav_folder_$name",
                name = name,
                type = FavoriteType.FOLDER,
                path = path,
                category = "Carpetas"
            )
        )
    }

    // Archivos favoritos (simulados)
    val favoriteFiles = listOf(
        "Presentación importante.pdf" to "/storage/emulated/0/Documents/Presentación importante.pdf",
        "Playlist favorita.m3u" to "/storage/emulated/0/Music/Playlist favorita.m3u",
        "Contactos backup.vcf" to "/storage/emulated/0/Contactos backup.vcf"
    )

    favoriteFiles.forEach { (name, path) ->
        items.add(
            FavoriteItem(
                id = "fav_file_${name.replace(" ", "_")}",
                name = name,
                type = FavoriteType.FILE,
                path = path,
                category = "Archivos"
            )
        )
    }

    return items.sortedBy { it.name }
}