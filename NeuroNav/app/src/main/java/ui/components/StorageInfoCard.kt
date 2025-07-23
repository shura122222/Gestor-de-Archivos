package com.finesi.neuronav.ui.components

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finesi.neuronav.ui.theme.*
import kotlin.math.roundToInt

data class StorageInfo(
    val totalSpace: Long,
    val usedSpace: Long,
    val freeSpace: Long,
    val usagePercentage: Float
)

@Composable
fun StorageInfoCard() {
    val storageInfo = remember { getStorageInfo() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = NeuroBlue.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeuroBlue.copy(alpha = 0.3f),
                            NeuroBlue.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Almacenamiento",
                        tint = NeuroBlue,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Almacenamiento",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estado del almacenamiento
                Text(
                    text = "Ocupado",
                    fontSize = 14.sp,
                    color = TextSecondary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatStorage(storageInfo.usedSpace),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.displayMedium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "/ ${formatStorage(storageInfo.totalSpace)}",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de progreso
                LinearProgressIndicator(
                    progress = { storageInfo.usagePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = when {
                        storageInfo.usagePercentage > 0.9f -> NeuroPurple
                        storageInfo.usagePercentage > 0.7f -> NeuroOrange
                        else -> NeuroBlue
                    },
                    trackColor = DarkCard,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Información adicional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(storageInfo.usagePercentage * 100).roundToInt()}% usado",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "${formatStorage(storageInfo.freeSpace)} libres",
                        fontSize = 12.sp,
                        color = NeuroGreen,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Función para obtener información del almacenamiento
fun getStorageInfo(): StorageInfo {
    return try {
        val internalStorage = Environment.getDataDirectory()
        val stat = StatFs(internalStorage.path)

        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalSpace = totalBlocks * blockSize
        val freeSpace = availableBlocks * blockSize
        val usedSpace = totalSpace - freeSpace
        val usagePercentage = if (totalSpace > 0) usedSpace.toFloat() / totalSpace.toFloat() else 0f

        StorageInfo(
            totalSpace = totalSpace,
            usedSpace = usedSpace,
            freeSpace = freeSpace,
            usagePercentage = usagePercentage
        )
    } catch (e: Exception) {
        // Valores por defecto en caso de error
        StorageInfo(
            totalSpace = 128L * 1024 * 1024 * 1024, // 128 GB
            usedSpace = 90L * 1024 * 1024 * 1024,   // 90 GB
            freeSpace = 38L * 1024 * 1024 * 1024,   // 38 GB
            usagePercentage = 0.7f                   // 70%
        )
    }
}

// Función para formatear el tamaño del almacenamiento
fun formatStorage(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        "%.1f GB".format(gb)
    } else {
        val mb = bytes / (1024.0 * 1024.0)
        "%.0f MB".format(mb)
    }
}