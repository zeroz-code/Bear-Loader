package com.bearmod.loader.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * Utility class for checking installed package versions
 * Specifically designed for PUBG Mobile variant detection and version comparison
 */
class PackageVersionChecker(private val context: Context) {

    companion object {
        /**
         * Official PUBG Mobile package names for different regions
         * These are the actual package names used by PUBG Mobile variants
         */
        const val PUBG_GLOBAL = "com.tencent.ig"
        const val PUBG_KR = "com.pubg.krmobile"
        const val PUBG_TW = "com.rekoo.pubgm"
        const val PUBG_VNG = "com.vng.pubgmobile"
        const val BGMI = "com.pubg.imobile"

        /**
         * Map variant IDs to their package names
         */
        val VARIANT_PACKAGE_MAP = mapOf(
            "pubg_global" to PUBG_GLOBAL,
            "pubg_kr" to PUBG_KR,
            "pubg_tw" to PUBG_TW,
            "pubg_vng" to PUBG_VNG,
            "bgmi" to BGMI
        )
    }

    /**
     * Check if a package is installed on the device
     */
    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get the installed version of a package
     * Returns null if package is not installed
     */
    fun getInstalledVersion(packageName: String): String? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Get the installed version code of a package
     * Returns -1 if package is not installed
     */
    fun getInstalledVersionCode(packageName: String): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            -1L
        }
    }

    /**
     * Compare two version strings
     * Returns: 
     * - Positive number if version1 > version2
     * - Negative number if version1 < version2  
     * - 0 if versions are equal
     */
    fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until maxLength) {
            val v1Part = v1Parts.getOrNull(i) ?: 0
            val v2Part = v2Parts.getOrNull(i) ?: 0
            
            when {
                v1Part > v2Part -> return 1
                v1Part < v2Part -> return -1
            }
        }
        
        return 0
    }

    /**
     * Check if an installed version is older than the available version
     */
    fun isUpdateAvailable(packageName: String, availableVersion: String): Boolean {
        val installedVersion = getInstalledVersion(packageName) ?: return false
        return compareVersions(installedVersion, availableVersion) < 0
    }

    /**
     * Get package information for PUBG variant
     */
    fun getPubgVariantInfo(variantId: String): PubgPackageInfo? {
        val packageName = VARIANT_PACKAGE_MAP[variantId] ?: return null
        
        return if (isPackageInstalled(packageName)) {
            PubgPackageInfo(
                packageName = packageName,
                isInstalled = true,
                installedVersion = getInstalledVersion(packageName),
                installedVersionCode = getInstalledVersionCode(packageName)
            )
        } else {
            PubgPackageInfo(
                packageName = packageName,
                isInstalled = false,
                installedVersion = null,
                installedVersionCode = -1L
            )
        }
    }

    /**
     * Launch an installed PUBG variant
     */
    fun launchPubgVariant(variantId: String): Boolean {
        val packageName = VARIANT_PACKAGE_MAP[variantId] ?: return false
        
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Data class to hold package information
 */
data class PubgPackageInfo(
    val packageName: String,
    val isInstalled: Boolean,
    val installedVersion: String?,
    val installedVersionCode: Long
)
