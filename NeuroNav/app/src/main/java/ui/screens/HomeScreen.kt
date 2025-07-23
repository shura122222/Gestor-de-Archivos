package com.finesi.neuronav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.finesi.neuronav.ui.components.GestureHandler
import com.finesi.neuronav.ui.components.NeuralButton
import com.finesi.neuronav.ui.components.NeuralHeader
import com.finesi.neuronav.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFiles: () -> Unit = {},
    onNavigateToApps: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {}
) {
    GestureHandler(
        onSwipeLeft = { onNavigateToApps() },
        onSwipeRight = { onNavigateToFiles() },
        onSwipeUp = { onNavigateToSearch() }
    ) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Header principal
                NeuralHeader(
                    title = "NEURONAV",
                    subtitle = "Sistema de Navegación Neural"
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botones principales
                NeuralButton(
                    text = "EXPLORAR ARCHIVOS",
                    icon = Icons.Default.Folder,
                    onClick = onNavigateToFiles
                )

                NeuralButton(
                    text = "APLICACIONES",
                    icon = Icons.Default.Apps,
                    onClick = onNavigateToApps
                )

                NeuralButton(
                    text = "BÚSQUEDA NEURAL",
                    icon = Icons.Default.Search,
                    onClick = onNavigateToSearch
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Información del sistema
                SystemInfoCard()
            }
        }
    }
}

@Composable
fun SystemInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.7f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ESTADO DEL SISTEMA",
                color = NeuroGreen,
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CPU:",
                    color = TextSecondary
                )
                Text(
                    text = "NEURAL CORE ACTIVO",
                    color = NeuroBlue
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MEMORIA:",
                    color = TextSecondary
                )
                Text(
                    text = "OPTIMIZADA",
                    color = NeuroGreen
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CONEXIÓN:",
                    color = TextSecondary
                )
                Text(
                    text = "SINCRONIZADO",
                    color = NeuroBlue
                )
            }
        }
    }
}