package com.finesi.neuronav.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun getStorageDirectories(): List<File> {
        val directories = mutableListOf<File>()

        try {
            // Directorio principal de almacenamiento
            val externalStorage = Environment.getExternalStorageDirectory()
            if (externalStorage?.exists() == true) {
                directories.add(externalStorage)
            }

            // Directorios públicos específicos
            val publicDirs = listOf(
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_DOCUMENTS
            )

            publicDirs.forEach { dirType ->
                try {
                    val dir = Environment.getExternalStoragePublicDirectory(dirType)
                    if (dir?.exists() == true) {
                        directories.add(dir)
                    }
                } catch (e: Exception) {
                    // Ignorar errores de acceso
                }
            }

        } catch (e: Exception) {
            // Fallback: agregar al menos el directorio de descargas
            try {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadDir?.exists() == true) {
                    directories.add(downloadDir)
                }
            } catch (ex: Exception) {
                // Último fallback
            }
        }

        return directories.distinctBy { it.absolutePath }
    }

    fun getFilesInDirectory(directory: File): List<File> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        return try {
            directory.listFiles()?.toList()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = sizeInBytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return String.format("%.1f %s", size, units[unitIndex])
    }

    fun formatDate(lastModified: Long): String {
        val date = Date(lastModified)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }

    fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }

    fun openFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(file))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Abrir archivo"))
            }
        } catch (e: Exception) {
            // Error al abrir archivo
        }
    }

    fun getFileIcon(file: File): String {
        return when {
            file.isDirectory -> "📁"
            file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> "🖼️"
            file.extension.lowercase() in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv") -> "🎬"
            file.extension.lowercase() in listOf("mp3", "wav", "flac", "aac", "ogg") -> "🎵"
            file.extension.lowercase() in listOf("pdf") -> "📄"
            file.extension.lowercase() in listOf("txt", "doc", "docx", "rtf") -> "📝"
            file.extension.lowercase() in listOf("zip", "rar", "7z", "tar", "gz") -> "📦"
            file.extension.lowercase() in listOf("apk") -> "📱"
            file.extension.lowercase() in listOf("xls", "xlsx", "csv") -> "📊"
            file.extension.lowercase() in listOf("ppt", "pptx") -> "📈"
            else -> "📄"
        }
    }
}