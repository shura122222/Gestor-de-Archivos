package com.finesi.neuronav.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finesi.neuronav.ui.theme.*
import com.finesi.neuronav.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Estados para la gestión de archivos
data class FileOperationState(
    val copiedFiles: List<File> = emptyList(),
    val cutFiles: List<File> = emptyList(),
    val selectedFiles: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)

// Enum para operaciones de archivos
enum class FileOperation {
    COPY, CUT, DELETE, SHARE, EDIT, PROPERTIES
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileExplorerScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentDirectory by remember { mutableStateOf<File?>(null) }
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    var directoryHistory by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var fileOperationState by remember { mutableStateOf(FileOperationState()) }
    var showContextMenu by remember { mutableStateOf<File?>(null) }
    var showCleanupDialog by remember { mutableStateOf(false) }
    var showFileActionDialog by remember { mutableStateOf<Pair<File, FileOperation>?>(null) }

    // Cargar directorio inicial
    LaunchedEffect(Unit) {
        val initialDir = android.os.Environment.getExternalStorageDirectory()
        currentDirectory = initialDir
        files = FileUtils.getFilesInDirectory(initialDir)
        isLoading = false
    }

    // Función para navegar a un directorio
    fun navigateToDirectory(directory: File) {
        currentDirectory?.let { current ->
            directoryHistory = directoryHistory + current
        }
        currentDirectory = directory
        files = FileUtils.getFilesInDirectory(directory)
        fileOperationState = fileOperationState.copy(selectedFiles = emptySet(), isSelectionMode = false)
    }

    // Función para ir atrás
    fun goBack() {
        if (directoryHistory.isNotEmpty()) {
            val previousDirectory = directoryHistory.last()
            directoryHistory = directoryHistory.dropLast(1)
            currentDirectory = previousDirectory
            files = FileUtils.getFilesInDirectory(previousDirectory)
        } else {
            onBackClick()
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
            // Header con herramientas
            FileExplorerHeader(
                currentDirectory = currentDirectory,
                fileOperationState = fileOperationState,
                onBackClick = { goBack() },
                onHomeClick = {
                    val rootDir = android.os.Environment.getExternalStorageDirectory()
                    currentDirectory = rootDir
                    files = FileUtils.getFilesInDirectory(rootDir)
                    directoryHistory = emptyList()
                    fileOperationState = fileOperationState.copy(selectedFiles = emptySet(), isSelectionMode = false)
                },
                onCleanupClick = { showCleanupDialog = true },
                onSelectAllClick = {
                    val allFileNames = files.map { it.name }.toSet()
                    fileOperationState = fileOperationState.copy(
                        selectedFiles = if (fileOperationState.selectedFiles.size == files.size) emptySet() else allFileNames,
                        isSelectionMode = allFileNames.isNotEmpty()
                    )
                },
                onPasteClick = {
                    coroutineScope.launch {
                        performPasteOperation(context, currentDirectory, fileOperationState) { newState ->
                            fileOperationState = newState
                            files = FileUtils.getFilesInDirectory(currentDirectory!!)
                        }
                    }
                }
            )

            // Breadcrumb de navegación
            NavigationBreadcrumb(
                currentPath = currentDirectory?.absolutePath ?: "",
                onPathClick = { path ->
                    val newDir = File(path)
                    if (newDir.exists() && newDir.isDirectory) {
                        currentDirectory = newDir
                        files = FileUtils.getFilesInDirectory(newDir)
                    }
                }
            )

            // Lista de archivos
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeuroGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files) { file ->
                        FileItemWithActions(
                            file = file,
                            isSelected = fileOperationState.selectedFiles.contains(file.name),
                            isSelectionMode = fileOperationState.isSelectionMode,
                            onFileClick = {
                                if (fileOperationState.isSelectionMode) {
                                    val newSelection = if (file.name in fileOperationState.selectedFiles) {
                                        fileOperationState.selectedFiles - file.name
                                    } else {
                                        fileOperationState.selectedFiles + file.name
                                    }
                                    fileOperationState = fileOperationState.copy(
                                        selectedFiles = newSelection,
                                        isSelectionMode = newSelection.isNotEmpty()
                                    )
                                } else {
                                    if (file.isDirectory) {
                                        navigateToDirectory(file)
                                    } else {
                                        FileUtils.openFile(context, file)
                                    }
                                }
                            },
                            onFileLongClick = {
                                showContextMenu = file
                            }
                        )
                    }
                }
            }
        }

        // Menu contextual
        showContextMenu?.let { file ->
            FileContextMenu(
                file = file,
                onDismiss = { showContextMenu = null },
                onActionSelected = { action ->
                    showFileActionDialog = file to action
                    showContextMenu = null
                }
            )
        }

        // Diálogo de limpieza
        if (showCleanupDialog) {
            CleanupDialog(
                onDismiss = { showCleanupDialog = false },
                onConfirm = {
                    coroutineScope.launch {
                        performCleanup(context) {
                            files = FileUtils.getFilesInDirectory(currentDirectory!!)
                        }
                    }
                    showCleanupDialog = false
                }
            )
        }

        // Diálogo de acciones de archivo
        showFileActionDialog?.let { (file, action) ->
            FileActionDialog(
                file = file,
                action = action,
                onDismiss = { showFileActionDialog = null },
                onConfirm = { confirmedFile, confirmedAction ->
                    coroutineScope.launch {
                        performFileAction(context, confirmedFile, confirmedAction, fileOperationState) { newState ->
                            fileOperationState = newState
                            files = FileUtils.getFilesInDirectory(currentDirectory!!)
                        }
                    }
                    showFileActionDialog = null
                }
            )
        }
    }
}

@Composable
fun FileExplorerHeader(
    currentDirectory: File?,
    fileOperationState: FileOperationState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCleanupClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onPasteClick: () -> Unit
) {
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
            // Primera fila - Navegación básica
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = NeuroGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onHomeClick) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Inicio",
                        tint = NeuroBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXPLORADOR NEURAL",
                        color = NeuroGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentDirectory?.name ?: "Cargando...",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onCleanupClick) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Limpiar basura",
                        tint = NeuroOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segunda fila - Herramientas de archivo
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FileToolButton(
                        icon = Icons.Default.SelectAll,
                        text = if (fileOperationState.selectedFiles.isEmpty()) "Seleccionar" else "Deseleccionar",
                        color = NeuroPurple,
                        onClick = onSelectAllClick
                    )
                }

                if (fileOperationState.copiedFiles.isNotEmpty() || fileOperationState.cutFiles.isNotEmpty()) {
                    item {
                        FileToolButton(
                            icon = Icons.Default.ContentPaste,
                            text = "Pegar",
                            color = NeuroGreen,
                            onClick = onPasteClick
                        )
                    }
                }

                if (fileOperationState.selectedFiles.isNotEmpty()) {
                    item {
                        Text(
                            text = "${fileOperationState.selectedFiles.size} seleccionados",
                            color = TextAccent,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileToolButton(
    icon: ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NavigationBreadcrumb(
    currentPath: String,
    onPathClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val pathParts = currentPath.split("/").filter { it.isNotEmpty() }
        items(pathParts.size) { index ->
            val partialPath = "/" + pathParts.take(index + 1).joinToString("/")
            val displayName = if (index == 0) "Root" else pathParts[index]

            Text(
                text = displayName,
                color = NeuroBlue,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { onPathClick(partialPath) }
                    .background(
                        NeuroBlue.copy(alpha = 0.2f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            if (index < pathParts.size - 1) {
                Text(
                    text = " › ",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemWithActions(
    file: File,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onFileClick: () -> Unit,
    onFileLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onFileClick,
                onLongClick = onFileLongClick
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        NeuroBlue,
                        RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier.border(
                        1.dp,
                        if (file.isDirectory) NeuroGreen.copy(alpha = 0.3f) else NeuroBlue.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                NeuroBlue.copy(alpha = 0.2f)
            else
                DarkCard.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox en modo selección
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onFileClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NeuroBlue,
                        uncheckedColor = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Icono del archivo
            Text(
                text = FileUtils.getFileIcon(file),
                fontSize = 24.sp,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del archivo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!file.isDirectory) {
                    Text(
                        text = "${FileUtils.formatFileSize(file.length())} • ${formatFileDate(file.lastModified())}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    val itemCount = try {
                        file.listFiles()?.size ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    Text(
                        text = "$itemCount elementos • ${formatFileDate(file.lastModified())}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Indicador visual
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.ChevronRight else Icons.Default.MoreVert,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FileContextMenu(
    file: File,
    onDismiss: () -> Unit,
    onActionSelected: (FileOperation) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = file.name,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column {
                FileContextMenuItem(
                    icon = Icons.Default.ContentCopy,
                    text = "Copiar",
                    onClick = { onActionSelected(FileOperation.COPY) }
                )
                FileContextMenuItem(
                    icon = Icons.Default.ContentCut,
                    text = "Cortar",
                    onClick = { onActionSelected(FileOperation.CUT) }
                )
                if (!file.isDirectory) {
                    FileContextMenuItem(
                        icon = Icons.Default.Edit,
                        text = "Editar",
                        onClick = { onActionSelected(FileOperation.EDIT) }
                    )
                }
                FileContextMenuItem(
                    icon = Icons.Default.Share,
                    text = "Compartir",
                    onClick = { onActionSelected(FileOperation.SHARE) }
                )
                FileContextMenuItem(
                    icon = Icons.Default.Info,
                    text = "Propiedades",
                    onClick = { onActionSelected(FileOperation.PROPERTIES) }
                )
                FileContextMenuItem(
                    icon = Icons.Default.Delete,
                    text = "Eliminar",
                    color = NeuroPurple,
                    onClick = { onActionSelected(FileOperation.DELETE) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = DarkCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
fun FileContextMenuItem(
    icon: ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = color,
            fontSize = 16.sp
        )
    }
}

@Composable
fun CleanupDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = NeuroOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Limpiar Basura", color = TextPrimary)
            }
        },
        text = {
            Column {
                Text("Se eliminarán los siguientes archivos:", color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Archivos temporales (.tmp)", color = TextSecondary)
                Text("• Caché de aplicaciones", color = TextSecondary)
                Text("• Archivos de log (.log)", color = TextSecondary)
                Text("• Papelera de reciclaje", color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Esta acción no se puede deshacer.",
                    color = NeuroOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Limpiar", color = NeuroOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = DarkCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
fun FileActionDialog(
    file: File,
    action: FileOperation,
    onDismiss: () -> Unit,
    onConfirm: (File, FileOperation) -> Unit
) {
    when (action) {
        FileOperation.PROPERTIES -> {
            FilePropertiesDialog(file = file, onDismiss = onDismiss)
        }
        FileOperation.DELETE -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text("Confirmar eliminación", color = TextPrimary)
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas eliminar \"${file.name}\"?",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm(file, action) }) {
                        Text("Eliminar", color = NeuroPurple)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = DarkCard
            )
        }
        else -> {
            // Para otras acciones, ejecutar directamente
            LaunchedEffect(Unit) {
                onConfirm(file, action)
            }
        }
    }
}

@Composable
fun FilePropertiesDialog(
    file: File,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Propiedades", color = TextPrimary)
        },
        text = {
            Column {
                PropertyRow("Nombre:", file.name)
                PropertyRow("Tipo:", if (file.isDirectory) "Carpeta" else "Archivo")
                PropertyRow("Tamaño:", if (file.isDirectory) "${file.listFiles()?.size ?: 0} elementos"
                else FileUtils.formatFileSize(file.length()))
                PropertyRow("Ubicación:", file.parent ?: "")
                PropertyRow("Modificado:", formatFileDate(file.lastModified()))
                PropertyRow("Permisos:",
                    "${if (file.canRead()) "R" else "-"}${if (file.canWrite()) "W" else "-"}${if (file.canExecute()) "X" else "-"}")
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
fun PropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// Funciones auxiliares
fun formatFileDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

suspend fun performFileAction(
    context: Context,
    file: File,
    action: FileOperation,
    currentState: FileOperationState,
    onStateUpdate: (FileOperationState) -> Unit
) {
    when (action) {
        FileOperation.COPY -> {
            onStateUpdate(currentState.copy(copiedFiles = listOf(file), cutFiles = emptyList()))
            Toast.makeText(context, "Archivo copiado", Toast.LENGTH_SHORT).show()
        }
        FileOperation.CUT -> {
            onStateUpdate(currentState.copy(cutFiles = listOf(file), copiedFiles = emptyList()))
            Toast.makeText(context, "Archivo cortado", Toast.LENGTH_SHORT).show()
        }
        FileOperation.DELETE -> {
            try {
                if (file.delete()) {
                    Toast.makeText(context, "Archivo eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al eliminar archivo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        FileOperation.SHARE -> {
            try {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = FileUtils.getMimeType(file)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir archivo"))
            } catch (e: Exception) {
                Toast.makeText(context, "Error al compartir: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        FileOperation.EDIT -> {
            FileUtils.openFile(context, file)
        }
        FileOperation.PROPERTIES -> {
            // Manejado en el diálogo
        }
    }
}

suspend fun performPasteOperation(
    context: Context,
    currentDirectory: File?,
    currentState: FileOperationState,
    onStateUpdate: (FileOperationState) -> Unit
) {
    if (currentDirectory == null) return

    try {
        val filesToProcess = currentState.copiedFiles + currentState.cutFiles
        val isCutOperation = currentState.cutFiles.isNotEmpty()

        for (file in filesToProcess) {
            val destinationFile = File(currentDirectory, file.name)
            if (file.exists()) {
                if (isCutOperation) {
                    // Mover archivo
                    if (file.renameTo(destinationFile)) {
                        Toast.makeText(context, "Archivo movido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Copiar archivo
                    file.copyTo(destinationFile, overwrite = false)
                    Toast.makeText(context, "Archivo copiado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        onStateUpdate(currentState.copy(copiedFiles = emptyList(), cutFiles = emptyList()))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al pegar: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

suspend fun performCleanup(
    context: Context,
    onComplete: () -> Unit
) {
    try {
        var deletedCount = 0
        val directories = listOf(
            android.os.Environment.getExternalStorageDirectory(),
            context.cacheDir,
            context.filesDir
        )

        for (directory in directories) {
            directory.walkTopDown().forEach { file ->
                try {
                    // Eliminar archivos temporales y de caché
                    when {
                        file.extension.lowercase() in listOf("tmp", "temp", "cache", "log") -> {
                            if (file.delete()) deletedCount++
                        }
                        file.name.startsWith(".") && file.extension.lowercase() in listOf("tmp", "log") -> {
                            if (file.delete()) deletedCount++
                        }
                        file.parentFile?.name == "cache" && file.isFile -> {
                            if (file.delete()) deletedCount++
                        }
                    }
                } catch (e: Exception) {
                    // Ignorar errores individuales
                }
            }
        }

        onComplete()
        Toast.makeText(context, "Limpieza completada: $deletedCount archivos eliminados", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error en la limpieza: ${e.message}", Toast.LENGTH_SHORT).show()
        onComplete()
    }
}