package com.keyauth.loader.data.model

import androidx.annotation.DrawableRes

/**
 * Button states for PUBG variant download/update/launch actions
 */
enum class PubgButtonState {
    DOWNLOAD,    // App not installed
    UPDATE,      // Older version installed
    OPEN,        // Current or newer version installed
    INSTALLING   // Currently installing/downloading
}

/**
 * Data class representing a PUBG Mobile variant
 * Used for displaying PUBG versions in ZEUS God Aim UI style
 * Enhanced with version checking and smart button states
 */
data class PubgVariant(
    val id: String,
    val name: String,
    val version: String,
    val size: String,
    @DrawableRes val iconRes: Int,
    val packageName: String,
    val downloadUrl: String = "",
    val buttonState: PubgButtonState = PubgButtonState.DOWNLOAD,
    val installedVersion: String? = null,
    val downloadProgress: Int = 0
) {
    companion object {
        /**
         * Get all available PUBG Mobile variants with package names
         * Enhanced to support version checking and smart button states
         */
        fun getAllVariants(): List<PubgVariant> {
            return listOf(
                PubgVariant(
                    id = "pubg_global",
                    name = "PUBG MOBILE",
                    version = "3.8.0",
                    size = "1.08 GB",
                    iconRes = com.keyauth.loader.R.drawable.ic_pubg_gl,
                    packageName = "com.tencent.ig"
                ),
                PubgVariant(
                    id = "pubg_kr",
                    name = "PUBG MOBILE KR",
                    version = "3.8.0",
                    size = "1.12 GB",
                    iconRes = com.keyauth.loader.R.drawable.ic_pubg_kr,
                    packageName = "com.pubg.krmobile"
                ),
                PubgVariant(
                    id = "pubg_tw",
                    name = "PUBG MOBILE TW",
                    version = "3.8.0",
                    size = "1.08 GB",
                    iconRes = com.keyauth.loader.R.drawable.ic_pubg_tw,
                    packageName = "com.rekoo.pubgm"
                ),
                PubgVariant(
                    id = "pubg_vng",
                    name = "PUBG MOBILE VNG",
                    version = "3.8.0",
                    size = "1.13 GB",
                    iconRes = com.keyauth.loader.R.drawable.ic_pubg_vng,
                    packageName = "com.vng.pubgmobile"
                ),
                PubgVariant(
                    id = "bgmi",
                    name = "BGMI",
                    version = "3.8.0",
                    size = "1.05 GB",
                    iconRes = com.keyauth.loader.R.drawable.battleground_mobile_india,
                    packageName = "com.pubg.imobile"
                )
            )
        }
    }
}
