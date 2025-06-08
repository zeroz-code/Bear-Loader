package com.keyauth.loader.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Utility class to handle migration of preferences between different storage implementations
 * This ensures that existing user data is preserved when updating from deprecated
 * AndroidX Security Crypto APIs to direct Android Keystore usage
 */
object PreferencesMigration {

    private const val TAG = "PreferencesMigration"
    private const val MIGRATION_COMPLETED_KEY = "migration_v2_completed"
    
    /**
     * Migrates preferences from old implementation to new implementation if needed
     * @param context Application context
     * @param newPreferences The new SecurePreferences instance
     */
    fun migrateIfNeeded(context: Context, newPreferences: SecurePreferences) {
        try {
            // Check if migration has already been completed
            val migrationPrefs = context.getSharedPreferences("migration_status", Context.MODE_PRIVATE)
            if (migrationPrefs.getBoolean(MIGRATION_COMPLETED_KEY, false)) {
                Log.d(TAG, "Migration already completed, skipping")
                return
            }
            
            // Try to access old preferences and migrate data
            val oldData = tryGetOldPreferences(context)
            if (oldData.isNotEmpty()) {
                Log.i(TAG, "Found old preferences data, starting migration")
                migrateData(oldData, newPreferences)
                
                // Mark migration as completed
                migrationPrefs.edit()
                    .putBoolean(MIGRATION_COMPLETED_KEY, true)
                    .apply()
                
                Log.i(TAG, "Migration completed successfully")
            } else {
                // No old data found, mark migration as completed anyway
                migrationPrefs.edit()
                    .putBoolean(MIGRATION_COMPLETED_KEY, true)
                    .apply()
                
                Log.d(TAG, "No old preferences data found, marking migration as completed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during preferences migration", e)
            // Don't fail the app if migration fails, just log the error
        }
    }
    
    /**
     * Attempts to retrieve data from old preferences implementations
     */
    private fun tryGetOldPreferences(context: Context): Map<String, Any> {
        val oldData = mutableMapOf<String, Any>()

        // List of old preference files to check for migration
        val oldPreferenceFiles = listOf(
            "keyauth_secure_prefs",           // Original encrypted preferences
            "keyauth_secure_prefs_fallback",  // Fallback unencrypted preferences
            "keyauth_secure_prefs_v2_fallback" // V2 fallback preferences
        )

        for (prefsName in oldPreferenceFiles) {
            try {
                Log.d(TAG, "Checking for data in: $prefsName")
                val oldPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

                // Check if this preferences file has any data
                if (oldPrefs.all.isEmpty()) {
                    Log.d(TAG, "No data found in $prefsName")
                    continue
                }

                // Extract license key
                oldPrefs.getString("license_key", null)?.let { licenseKey ->
                    if (licenseKey.isNotBlank()) {
                        oldData["license_key"] = licenseKey
                        Log.d(TAG, "Found license key in $prefsName")
                    }
                }

                // Extract remember license setting
                if (oldPrefs.contains("remember_license")) {
                    oldData["remember_license"] = oldPrefs.getBoolean("remember_license", false)
                    Log.d(TAG, "Found remember license setting in $prefsName")
                }

                // Extract auto login setting
                if (oldPrefs.contains("auto_login")) {
                    oldData["auto_login"] = oldPrefs.getBoolean("auto_login", false)
                    Log.d(TAG, "Found auto login setting in $prefsName")
                }

                // If we found data, we can stop checking other files
                if (oldData.isNotEmpty()) {
                    Log.i(TAG, "Successfully retrieved ${oldData.size} items from $prefsName")
                    break
                }

            } catch (e: Exception) {
                Log.w(TAG, "Could not access preferences file: $prefsName", e)
                // Continue to next file
            }
        }

        if (oldData.isEmpty()) {
            Log.d(TAG, "No migration data found in any old preferences files")
        }

        return oldData
    }
    
    /**
     * Migrates the old data to the new preferences implementation
     */
    private fun migrateData(oldData: Map<String, Any>, newPreferences: SecurePreferences) {
        try {
            oldData["license_key"]?.let { licenseKey ->
                if (licenseKey is String && licenseKey.isNotBlank()) {
                    newPreferences.saveLicenseKey(licenseKey)
                    Log.d(TAG, "Migrated license key")
                }
            }
            
            oldData["remember_license"]?.let { rememberLicense ->
                if (rememberLicense is Boolean) {
                    newPreferences.setRememberLicense(rememberLicense)
                    Log.d(TAG, "Migrated remember license setting: $rememberLicense")
                }
            }
            
            oldData["auto_login"]?.let { autoLogin ->
                if (autoLogin is Boolean) {
                    newPreferences.setAutoLogin(autoLogin)
                    Log.d(TAG, "Migrated auto login setting: $autoLogin")
                }
            }
            
            Log.i(TAG, "Successfully migrated all preference data")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating preference data", e)
            throw e
        }
    }
    
    /**
     * Clears migration status (for testing purposes)
     */
    fun resetMigrationStatus(context: Context) {
        val migrationPrefs = context.getSharedPreferences("migration_status", Context.MODE_PRIVATE)
        migrationPrefs.edit().clear().apply()
        Log.d(TAG, "Migration status reset")
    }
}
