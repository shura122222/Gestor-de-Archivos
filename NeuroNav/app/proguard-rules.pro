# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================================
# OPTIMIZACIONES PARA REDUCIR TAMAÑO DEL APK
# ============================================================================

# Configuraciones de optimización
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Habilitar obfuscación más agresiva
-repackageclasses ''
-allowaccessmodification

# ============================================================================
# MANTENER CLASES ESENCIALES
# ============================================================================

# Mantener clases de Compose (esencial para tu app)
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.animation.** { *; }

# Mantener Kotlin y Corrutinas
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Mantener ViewModels y LiveData
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }

# Mantener clases de tu aplicación (cambia com.finesi.neuronav por tu paquete)
-keep class com.finesi.neuronav.** { *; }

# Mantener Parcelable (usas kotlin-parcelize)
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ============================================================================
# ELIMINAR CÓDIGO DE DEBUG Y LOGGING
# ============================================================================

# Remover todos los logs en release (reduce mucho el tamaño)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
    public static int wtf(...);
}

# Remover print statements de Kotlin
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static void println(...);
    public static void print(...);
}

# ============================================================================
# MANTENER ATRIBUTOS NECESARIOS
# ============================================================================

# Mantener anotaciones importantes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Para debugging (puedes comentar en producción final)
-keepattributes SourceFile,LineNumberTable

# ============================================================================
# REGLAS ESPECÍFICAS PARA TUS DEPENDENCIAS
# ============================================================================

# Accompanist Permissions
-keep class com.google.accompanist.permissions.** { *; }

# Coil (para cargar imágenes)
-keep class coil.** { *; }

# DocumentFile
-keep class androidx.documentfile.** { *; }

# ============================================================================
# REGLAS ADICIONALES PARA REDUCIR TAMAÑO
# ============================================================================

# Eliminar clases no utilizadas más agresivamente
-dontwarn **
-ignorewarnings

# Si tu proyecto usa WebView con JS, descomenta las siguientes líneas:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Ocultar nombres de archivos fuente originales
#-renamesourcefileattribute SourceFile