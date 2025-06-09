package com.bearmod.loader.config

/**
 * KeyAuth configuration constants
 *
 * IMPORTANT: Replace these values with your actual KeyAuth application details
 * Get these from your KeyAuth dashboard at https://keyauth.cc/app/
 */
object KeyAuthConfig {

    /**
     * Your application name from KeyAuth dashboard
     * Found on the main application page
     */
    const val APP_NAME = "com.bearmod.loader"

    /**
     * Your owner ID from KeyAuth account settings
     * Click your profile picture -> Account Settings
     */
    const val OWNER_ID = "yLoA9zcOEF"

    /**
     * Your application version
     * Should match the version in your KeyAuth dashboard
     */
    const val APP_VERSION = "1.3"

    /**
     * Custom KeyAuth hash for integrity checking
     * This hash is used for additional security validation
     */
    const val CUSTOM_HASH = "4f9b15598f6e8bdf07ca39e9914cd3e9"
    
    /**
     * KeyAuth API base URL
     * For API v1.3, use: https://keyauth.win/api/1.3/
     * You can also use your custom domain if configured
     */
    const val API_BASE_URL = "https://keyauth.win/api/1.3/"
    
    /**
     * Application display name for UI
     */
    const val APP_DISPLAY_NAME = "BEAR-MOD"
    
    /**
     * Application version for display
     */
    const val APP_DISPLAY_VERSION = "1.0.6"

    /**
     * OTA Update System Configuration
     */

    /**
     * JSON endpoint for version information
     * Should return version, build, and variants information
     */
    const val OTA_VERSION_ENDPOINT = "https://api.github.com/repos/your-username/your-repo/releases/latest"

    /**
     * Current local version for comparison
     */
    const val CURRENT_VERSION = 3

    /**
     * Current build number
     */
    const val CURRENT_BUILD = 1

    /**
     * Available variants - Simplified to main PUBG Mobile versions only
     * GL = Global, KR = Korea, TW = Taiwan, VNG = Vietnam, BGMI = Battlegrounds Mobile India
     */
    val AVAILABLE_VARIANTS = listOf("GL", "KR", "TW", "VNG", "BGMI")

    /**
     * Default variant
     */
    const val DEFAULT_VARIANT = "GL"
}
