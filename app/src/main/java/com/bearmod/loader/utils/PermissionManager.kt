package com.bearmod.loader.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Enhanced utility class for managing permissions required for OTA updates
 * Supports Android 11+ MANAGE_EXTERNAL_STORAGE and legacy storage permissions
 */
class PermissionManager(private val context: Context) {

    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 1001
        const val REQUEST_CODE_INSTALL_PERMISSION = 1002
        const val REQUEST_CODE_ALL_PERMISSIONS = 1003
        const val REQUEST_MANAGE_EXTERNAL_STORAGE = 1004
        const val REQUEST_INSTALL_PACKAGES = REQUEST_CODE_INSTALL_PERMISSION
    }
    
    /**
     * Check if all required permissions are granted
     */
    fun hasAllRequiredPermissions(): Boolean {
        return hasStoragePermission() && hasInstallPermission()
    }
    
    /**
     * Check if storage permission is granted (Enhanced for Android 11+)
     */
    fun hasStoragePermission(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ doesn't require WRITE_EXTERNAL_STORAGE for app-specific directories
                true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+ requires MANAGE_EXTERNAL_STORAGE for full access
                Environment.isExternalStorageManager()
            }
            else -> {
                // Android 10 and below use WRITE_EXTERNAL_STORAGE
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
    
    /**
     * Check if install permission is granted
     */
    fun hasInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            // For API < 26, check INSTALL_PACKAGES permission (system permission)
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.INSTALL_PACKAGES
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Request storage permission (Enhanced for Android 11+)
     */
    fun requestStoragePermission(activity: Activity) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ - No permission needed for app-specific directories
                return
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+ - Request MANAGE_EXTERNAL_STORAGE
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${activity.packageName}")
                    }
                    activity.startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE)
                } catch (e: Exception) {
                    // Fallback to general manage all files permission
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    activity.startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE)
                }
            }
            else -> {
                // Android 10 and below - Request WRITE_EXTERNAL_STORAGE
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            }
        }
    }

    /**
     * Request storage permission from Fragment (Enhanced for Android 11+)
     */
    fun requestStoragePermission(fragment: Fragment) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ - No permission needed for app-specific directories
                return
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+ - Request MANAGE_EXTERNAL_STORAGE
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${fragment.requireContext().packageName}")
                    }
                    fragment.startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE)
                } catch (e: Exception) {
                    // Fallback to general manage all files permission
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    fragment.startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE)
                }
            }
            else -> {
                // Android 10 and below - Request WRITE_EXTERNAL_STORAGE
                fragment.requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            }
        }
    }
    
    /**
     * Request install permission
     */
    fun requestInstallPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                activity.startActivityForResult(intent, REQUEST_CODE_INSTALL_PERMISSION)
            }
        }
    }
    
    /**
     * Request all required permissions
     */
    fun requestAllPermissions(activity: Activity) {
        val permissionsToRequest = mutableListOf<String>()
        
        // Add storage permission if needed
        if (!hasStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_ALL_PERMISSIONS
            )
        } else {
            // If no runtime permissions needed, check install permission
            requestInstallPermission(activity)
        }
    }
    
    /**
     * Handle permission request result (Enhanced for Android 11+)
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onAllGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION,
            REQUEST_CODE_ALL_PERMISSIONS -> {
                val deniedPermissions = mutableListOf<String>()

                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i])
                    }
                }

                if (deniedPermissions.isEmpty()) {
                    // Check if we still need install permission
                    if (hasAllRequiredPermissions()) {
                        onAllGranted()
                    } else {
                        // Request install permission if needed
                        if (context is Activity) {
                            requestInstallPermission(context)
                        }
                    }
                } else {
                    onDenied(deniedPermissions)
                }
            }
        }
    }

    /**
     * Handle activity result for Android 11+ permission requests
     */
    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        onAllGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        when (requestCode) {
            REQUEST_MANAGE_EXTERNAL_STORAGE -> {
                if (hasStoragePermission()) {
                    if (hasAllRequiredPermissions()) {
                        onAllGranted()
                    } else {
                        // Still need install permission
                        if (context is Activity) {
                            requestInstallPermission(context)
                        }
                    }
                } else {
                    onDenied()
                }
            }
            REQUEST_CODE_INSTALL_PERMISSION -> {
                if (hasInstallPermission()) {
                    if (hasAllRequiredPermissions()) {
                        onAllGranted()
                    } else {
                        onDenied()
                    }
                } else {
                    onDenied()
                }
            }
        }
    }
    
    /**
     * Handle install permission result
     */
    fun handleInstallPermissionResult(
        requestCode: Int,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CODE_INSTALL_PERMISSION) {
            if (hasInstallPermission()) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
    
    /**
     * Get permission explanation text
     */
    fun getPermissionExplanation(): String {
        return buildString {
            appendLine("This app requires the following permissions to download and install updates:")
            appendLine()
            
            if (!hasStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                appendLine("• Storage Access: To download APK and OBB files")
            }
            
            if (!hasInstallPermission()) {
                appendLine("• Install Unknown Apps: To automatically install downloaded APK files")
            }
            
            appendLine()
            appendLine("These permissions are only used for the update process and ensure a seamless installation experience.")
        }
    }
    
    /**
     * Check if permission should show rationale
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Open app settings for manual permission grant
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        activity.startActivity(intent)
    }
}
