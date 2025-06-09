package com.bearmod.loader.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale

/**
 * Utility class for installing APK files
 */
class APKInstaller(private val context: Context) {
    
    /**
     * Install APK file
     */
    fun installAPK(apkFile: File): Boolean {
        return try {
            val intent = createInstallIntent(apkFile)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create install intent for APK file
     */
    private fun createInstallIntent(apkFile: File): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        
        // Use FileProvider for Android 7.0+ (minSdk is 24, so always use FileProvider)
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
    
    /**
     * Check if APK file is valid
     */
    fun isValidAPK(apkFile: File): Boolean {
        return try {
            apkFile.exists() && 
            apkFile.isFile && 
            apkFile.length() > 0 && 
            apkFile.name.endsWith(".apk", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get APK file size in human readable format
     */
    fun getAPKSize(apkFile: File): String {
        if (!apkFile.exists()) return "Unknown"
        
        val bytes = apkFile.length()
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format(Locale.ROOT, "%.2f GB", gb)
            mb >= 1 -> String.format(Locale.ROOT, "%.2f MB", mb)
            kb >= 1 -> String.format(Locale.ROOT, "%.2f KB", kb)
            else -> "$bytes bytes"
        }
    }
}
