package com.finesi.neuronav

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finesi.neuronav.ui.theme.NeuroNavTheme
import com.finesi.neuronav.ui.theme.ThemeManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "NeuroNav"
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Regresó de configuración de permisos")
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar el sistema de temas
        ThemeManager.initialize(this)

        Log.d(TAG, "MainActivity iniciada")

        setContent {
            NeuroNavTheme {
                Log.d(TAG, "Configurando permisos...")

                val permissionsState = rememberMultiplePermissionsState(
                    permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        listOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.VIBRATE
                        )
                    } else {
                        listOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.VIBRATE
                        )
                    }
                )

                var hasStorageManager by remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Environment.isExternalStorageManager()
                        } else {
                            true
                        }
                    )
                }

                LaunchedEffect(permissionsState.allPermissionsGranted) {
                    Log.d(TAG, "Permisos básicos: ${permissionsState.allPermissionsGranted}")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        hasStorageManager = Environment.isExternalStorageManager()
                        Log.d(TAG, "Storage Manager: $hasStorageManager")
                    }
                }

                val canUseApp = permissionsState.allPermissionsGranted && hasStorageManager
                Log.d(TAG, "Puede usar app: $canUseApp")

                if (canUseApp) {
                    Log.d(TAG, "Mostrando NeuroNavigation")
                    NeuroNavigation()
                } else {
                    Log.d(TAG, "Mostrando pantalla de permisos")
                    TestPermissionScreen(
                        hasBasicPermissions = permissionsState.allPermissionsGranted,
                        hasStorageManager = hasStorageManager,
                        onRequestBasicPermissions = {
                            Log.d(TAG, "BOTÓN PRESIONADO - Solicitando permisos básicos")
                            try {
                                permissionsState.launchMultiplePermissionRequest()
                                Log.d(TAG, "Permisos solicitados exitosamente")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al solicitar permisos", e)
                            }
                        },
                        onRequestStorageManager = {
                            Log.d(TAG, "BOTÓN PRESIONADO - Solicitando Storage Manager")
                            requestStorageManagerPermission()
                        }
                    )
                }
            }
        }
    }

    private fun requestStorageManagerPermission() {
        try {
            Log.d(TAG, "Abriendo configuración de Storage Manager")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                storagePermissionLauncher.launch(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo configuración", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity onResume")
    }
}

@Composable
fun TestPermissionScreen(
    hasBasicPermissions: Boolean,
    hasStorageManager: Boolean,
    onRequestBasicPermissions: () -> Unit,
    onRequestStorageManager: () -> Unit
) {
    // Debug en Compose
    Log.d("NeuroNav", "Recomposición - Permisos básicos: $hasBasicPermissions, Storage: $hasStorageManager")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF0F0F23),
                        Color.Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Permisos NeuroNav",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Debug info
                Text(
                    text = "Debug: Básicos=$hasBasicPermissions, Storage=$hasStorageManager",
                    fontSize = 10.sp,
                    color = Color.Yellow,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón simple de prueba
                Button(
                    onClick = {
                        Log.d("NeuroNav", "¡CLICK DETECTADO!")
                        if (!hasBasicPermissions) {
                            Log.d("NeuroNav", "Ejecutando onRequestBasicPermissions")
                            onRequestBasicPermissions()
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasStorageManager) {
                            Log.d("NeuroNav", "Ejecutando onRequestStorageManager")
                            onRequestStorageManager()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red, // Rojo para que sea bien visible
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = when {
                            !hasBasicPermissions -> "CONCEDER PERMISOS BÁSICOS"
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasStorageManager -> "CONFIGURAR STORAGE"
                            else -> "ALGO ESTÁ MAL"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Texto de estado
                Text(
                    text = if (!hasBasicPermissions) {
                        "Necesitamos permisos básicos primero"
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasStorageManager) {
                        "Ahora configura el acceso completo"
                    } else {
                        "¿Por qué estamos aquí?"
                    },
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}