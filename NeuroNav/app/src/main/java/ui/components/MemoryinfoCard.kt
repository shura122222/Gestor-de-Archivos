package com.finesi.neuronav.ui.components

import android.app.ActivityManager
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
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

data class MemoryInfo(
    val totalMemory: Long,
    val usedMemory: Long,
    val availableMemory: Long,
    val usagePercentage: Float
)

@Composable
fun MemoryInfoCard() {
    val context = LocalContext.current
    var memoryInfo by remember { mutableStateOf(getMemoryInfo(context)) }

    // Actualizar cada 3 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            memoryInfo = getMemoryInfo(context)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = NeuroGreen.copy(alpha = 0.15f)
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
                            NeuroGreen.copy(alpha = 0.3f),
                            NeuroGreen.copy(alpha = 0.1f)
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
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Memoria RAM",
                        tint = NeuroGreen,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Memoria RAM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estado de la memoria
                Text(
                    text = "En uso",
                    fontSize = 14.sp,
                    color = TextSecondary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMemory(memoryInfo.usedMemory),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.displayMedium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "/ ${formatMemory(memoryInfo.totalMemory)}",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de progreso con animación
                val animatedProgress by animateFloatAsState(
                    targetValue = memoryInfo.usagePercentage,
                    animationSpec = tween(durationMillis = 800),
                    label = "memory_progress"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = when {
                        memoryInfo.usagePercentage > 0.85f -> NeuroPurple
                        memoryInfo.usagePercentage > 0.65f -> NeuroOrange
                        else -> NeuroGreen
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
                        text = "${(memoryInfo.usagePercentage * 100).roundToInt()}% usado",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "${formatMemory(memoryInfo.availableMemory)} disponible",
                        fontSize = 12.sp,
                        color = NeuroGreen,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Estado del rendimiento
                Text(
                    text = getPerformanceStatus(memoryInfo.usagePercentage),
                    fontSize = 11.sp,
                    color = getPerformanceColor(memoryInfo.usagePercentage),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Función para obtener información de la memoria
fun getMemoryInfo(context: Context): MemoryInfo {
    return try {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemory = memoryInfo.totalMem
        val availableMemory = memoryInfo.availMem
        val usedMemory = totalMemory - availableMemory
        val usagePercentage = if (totalMemory > 0) usedMemory.toFloat() / totalMemory.toFloat() else 0f

        MemoryInfo(
            totalMemory = totalMemory,
            usedMemory = usedMemory,
            availableMemory = availableMemory,
            usagePercentage = usagePercentage
        )
    } catch (e: Exception) {
        // Valores por defecto en caso de error
        MemoryInfo(
            totalMemory = 8L * 1024 * 1024 * 1024,   // 8 GB
            usedMemory = 4L * 1024 * 1024 * 1024,     // 4 GB
            availableMemory = 4L * 1024 * 1024 * 1024, // 4 GB
            usagePercentage = 0.5f                     // 50%
        )
    }
}

// Función para formatear la memoria
fun formatMemory(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        "%.1f GB".format(gb)
    } else {
        val mb = bytes / (1024.0 * 1024.0)
        "%.0f MB".format(mb)
    }
}

// Función para obtener el estado del rendimiento
fun getPerformanceStatus(usagePercentage: Float): String {
    return when {
        usagePercentage > 0.85f -> "• MEMORIA CRÍTICA - Cierra algunas apps"
        usagePercentage > 0.65f -> "• MEMORIA MODERADA - Rendimiento normal"
        else -> "• MEMORIA ÓPTIMA - Rendimiento excelente"
    }
}

// Función para obtener el color del estado
fun getPerformanceColor(usagePercentage: Float): androidx.compose.ui.graphics.Color {
    return when {
        usagePercentage > 0.85f -> NeuroPurple
        usagePercentage > 0.65f -> NeuroOrange
        else -> NeuroGreen
    }
}