package com.finesi.neuronav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finesi.neuronav.ui.theme.*
import com.finesi.neuronav.utils.AppInfo
import com.finesi.neuronav.utils.AppManager
import com.finesi.neuronav.utils.FileUtils
import java.io.File

sealed class SearchResult {
    data class AppResult(val app: AppInfo) : SearchResult()
    data class FileResult(val file: File) : SearchResult()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchType by remember { mutableStateOf("all") } // "all", "apps", "files"

    // Función de búsqueda
    fun performSearch(query: String) {
        if (query.length < 2) {
            searchResults = emptyList()
            return
        }

        isSearching = true
        val results = mutableListOf<SearchResult>()

        try {
            // Buscar aplicaciones
            if (searchType == "all" || searchType == "apps") {
                val apps = AppManager.getInstalledApps(context)
                apps.filter {
                    it.name.contains(query, ignoreCase = true)
                }.forEach { app ->
                    results.add(SearchResult.AppResult(app))
                }
            }

            // Buscar archivos (búsqueda básica)
            if (searchType == "all" || searchType == "files") {
                val directories = FileUtils.getStorageDirectories()
                directories.forEach { dir ->
                    try {
                        val files = FileUtils.getFilesInDirectory(dir)
                        files.filter { file ->
                            file.name.contains(query, ignoreCase = true)
                        }.take(10).forEach { file ->
                            results.add(SearchResult.FileResult(file))
                        }
                    } catch (e: Exception) {
                        // Ignorar errores de acceso
                    }
                }
            }
        } catch (e: Exception) {
            // Manejar errores
        }

        searchResults = results.take(50) // Limitar resultados
        isSearching = false
    }

    // Ejecutar búsqueda cuando cambie el query
    LaunchedEffect(searchQuery, searchType) {
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
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
            // Header de búsqueda
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
                                tint = NeuroBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "BÚSQUEDA NEURAL",
                            color = NeuroGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Buscar archivos y aplicaciones...",
                                color = TextSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = NeuroBlue
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeuroBlue,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                performSearch(searchQuery)
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtros
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = searchType == "all",
                            onClick = { searchType = "all" },
                            label = { Text("Todo") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuroBlue.copy(alpha = 0.3f),
                                selectedLabelColor = NeuroBlue
                            )
                        )
                        FilterChip(
                            selected = searchType == "apps",
                            onClick = { searchType = "apps" },
                            label = { Text("Apps") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuroGreen.copy(alpha = 0.3f),
                                selectedLabelColor = NeuroGreen
                            )
                        )
                        FilterChip(
                            selected = searchType == "files",
                            onClick = { searchType = "files" },
                            label = { Text("Archivos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuroPurple.copy(alpha = 0.3f),
                                selectedLabelColor = NeuroPurple
                            )
                        )
                    }
                }
            }

            // Resultados
            if (isSearching) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NeuroBlue
                    )
                }
            } else if (searchQuery.isEmpty()) {
                // Estado inicial
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Escribe para buscar",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (searchResults.isEmpty()) {
                // Sin resultados
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NeuroOrange,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sin resultados",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                // Lista de resultados
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { result ->
                        SearchResultItem(
                            result = result,
                            onClick = {
                                when (result) {
                                    is SearchResult.AppResult -> {
                                        AppManager.launchApp(context, result.app.packageName)
                                    }
                                    is SearchResult.FileResult -> {
                                        FileUtils.openFile(context, result.file)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = when (result) {
                    is SearchResult.AppResult -> NeuroBlue.copy(alpha = 0.3f)
                    is SearchResult.FileResult -> NeuroGreen.copy(alpha = 0.3f)
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
            // Icono del tipo
            Icon(
                imageVector = when (result) {
                    is SearchResult.AppResult -> Icons.Default.Apps
                    is SearchResult.FileResult -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = when (result) {
                    is SearchResult.AppResult -> NeuroBlue
                    is SearchResult.FileResult -> NeuroGreen
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                when (result) {
                    is SearchResult.AppResult -> {
                        Text(
                            text = result.app.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Aplicación",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    is SearchResult.FileResult -> {
                        Text(
                            text = result.file.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Archivo • ${FileUtils.formatFileSize(result.file.length())}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Icono de acción
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}