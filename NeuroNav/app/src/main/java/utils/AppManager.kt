package com.finesi.neuronav.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val versionName: String,
    val isSystemApp: Boolean,
    val installTime: Long,
    val lastUpdateTime: Long
)

object AppManager {

    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        try {
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            for (packageInfo in packages) {
                val applicationInfo = packageInfo.applicationInfo

                // Verificar que applicationInfo no sea null
                if (applicationInfo != null) {
                    // Filtrar solo apps que se pueden lanzar
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageInfo.packageName)
                    if (launchIntent != null) {
                        val appInfo = AppInfo(
                            name = applicationInfo.loadLabel(packageManager).toString(),
                            packageName = packageInfo.packageName,
                            icon = applicationInfo.loadIcon(packageManager),
                            versionName = packageInfo.versionName ?: "1.0",
                            isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                            installTime = packageInfo.firstInstallTime,
                            lastUpdateTime = packageInfo.lastUpdateTime
                        )
                        apps.add(appInfo)
                    }
                }
            }
        } catch (e: Exception) {
            // Manejar error
        }

        return apps.sortedBy { it.name.lowercase() }
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            // Manejar error
        }
    }

    fun getAppInfo(context: Context, packageName: String): AppInfo? {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val applicationInfo = packageInfo.applicationInfo

            if (applicationInfo != null) {
                AppInfo(
                    name = applicationInfo.loadLabel(packageManager).toString(),
                    packageName = packageName,
                    icon = applicationInfo.loadIcon(packageManager),
                    versionName = packageInfo.versionName ?: "1.0",
                    isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    installTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun formatInstallDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return format.format(date)
    }
}