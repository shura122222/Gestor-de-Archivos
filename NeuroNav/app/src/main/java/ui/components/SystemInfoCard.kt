package com.finesi.neuronav.ui.components

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finesi.neuronav.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class SystemInfo(
    val storageTotal: Long,
    val storageUsed: Long,
    val storageFree: Long,
    val storagePercentage: Float,
    val memoryTotal: Long,
    val memoryUsed: Long,
    val memoryAvailable: Long,
    val memoryPercentage: Float
)

@Composable
fun SystemInfoCard() {
    val context = LocalContext.current
    var systemInfo by remember { mutableStateOf(getSystemInfo(context)) }

    // Actualizar cada 3 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            systemInfo = getSystemInfo(context)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "Estado del Sistema",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Almacenamiento
            StorageSection(systemInfo)

            Spacer(modifier = Modifier.height(16.dp))

            // Memoria RAM
            MemorySection(systemInfo)
        }
    }
}

@Composable
fun StorageSection(systemInfo: SystemInfo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = "Almacenamiento",
            tint = NeuroBlue,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Almacenamiento",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "${formatBytes(systemInfo.storageUsed)} / ${formatBytes(systemInfo.storageTotal)}",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Barra de progreso del almacenamiento
    LinearProgressIndicator(
        progress = { systemInfo.storagePercentage },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = when {
            systemInfo.storagePercentage > 0.9f -> NeuroPurple
            systemInfo.storagePercentage > 0.7f -> NeuroOrange
            else -> NeuroBlue
        },
        trackColor = DarkSurface,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${(systemInfo.storagePercentage * 100).roundToInt()}% usado",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Text(
            text = "${formatBytes(systemInfo.storageFree)} libres",
            fontSize = 12.sp,
            color = NeuroBlue
        )
    }
}

@Composable
fun MemorySection(systemInfo: SystemInfo) {
    val animatedProgress by animateFloatAsState(
        targetValue = systemInfo.memoryPercentage,
        animationSpec = tween(durationMillis = 800),
        label = "memory_progress"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = "Memoria RAM",
            tint = NeuroGreen,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Memoria RAM",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "${formatBytes(systemInfo.memoryUsed)} / ${formatBytes(systemInfo.memoryTotal)}",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Barra de progreso de la memoria
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = when {
            systemInfo.memoryPercentage > 0.85f -> NeuroPurple
            systemInfo.memoryPercentage > 0.65f -> NeuroOrange
            else -> NeuroGreen
        },
        trackColor = DarkSurface,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${(systemInfo.memoryPercentage * 100).roundToInt()}% usado",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Text(
            text = "${formatBytes(systemInfo.memoryAvailable)} disponible",
            fontSize = 12.sp,
            color = NeuroGreen
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Estado del rendimiento
    Text(
        text = getMemoryPerformanceStatus(systemInfo.memoryPercentage),
        fontSize = 11.sp,
        color = getMemoryPerformanceColor(systemInfo.memoryPercentage),
        fontWeight = FontWeight.Medium
    )
}

// Función para obtener información completa del sistema
fun getSystemInfo(context: Context): SystemInfo {
    // Información de almacenamiento
    val (storageTotal, storageUsed, storageFree, storagePercentage) = try {
        val internalStorage = Environment.getDataDirectory()
        val stat = StatFs(internalStorage.path)

        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val total = totalBlocks * blockSize
        val free = availableBlocks * blockSize
        val used = total - free
        val percentage = if (total > 0) used.toFloat() / total.toFloat() else 0f

        listOf(total, used, free, percentage)
    } catch (e: Exception) {
        listOf(128L * 1024 * 1024 * 1024, 90L * 1024 * 1024 * 1024, 38L * 1024 * 1024 * 1024, 0.7f)
    }

    // Información de memoria
    val (memoryTotal, memoryUsed, memoryAvailable, memoryPercentage) = try {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val total = memoryInfo.totalMem
        val available = memoryInfo.availMem
        val used = total - available
        val percentage = if (total > 0) used.toFloat() / total.toFloat() else 0f

        listOf(total, used, available, percentage)
    } catch (e: Exception) {
        listOf(8L * 1024 * 1024 * 1024, 4L * 1024 * 1024 * 1024, 4L * 1024 * 1024 * 1024, 0.5f)
    }

    return SystemInfo(
        storageTotal = storageTotal as Long,
        storageUsed = storageUsed as Long,
        storageFree = storageFree as Long,
        storagePercentage = storagePercentage as Float,
        memoryTotal = memoryTotal as Long,
        memoryUsed = memoryUsed as Long,
        memoryAvailable = memoryAvailable as Long,
        memoryPercentage = memoryPercentage as Float
    )
}

// Función para formatear bytes
fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        "%.1f GB".format(gb)
    } else {
        val mb = bytes / (1024.0 * 1024.0)
        "%.0f MB".format(mb)
    }
}

// Función para obtener el estado del rendimiento de memoria
fun getMemoryPerformanceStatus(memoryPercentage: Float): String {
    return when {
        memoryPercentage > 0.85f -> "• MEMORIA CRÍTICA"
        memoryPercentage > 0.65f -> "• MEMORIA MODERADA"
        else -> "• MEMORIA ÓPTIMA"
    }
}

// Función para obtener el color del estado de memoria
fun getMemoryPerformanceColor(memoryPercentage: Float): androidx.compose.ui.graphics.Color {
    return when {
        memoryPercentage > 0.85f -> NeuroPurple
        memoryPercentage > 0.65f -> NeuroOrange
        else -> NeuroGreen
    }
}