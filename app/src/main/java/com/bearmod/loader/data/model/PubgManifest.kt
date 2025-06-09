package com.bearmod.loader.data.model

/**
 * JSON manifest data models for dynamic PUBG variant loading
 * Integrates with existing KeyAuth Loader smart button system
 */

/**
 * Root manifest containing all PUBG variant information
 */
data class PubgManifest(
    val version: String,
    val build: String,
    val variants: Map<String, PubgVariantInfo>
)

/**
 * Information for a specific PUBG variant
 */
data class PubgVariantInfo(
    val apk: PubgDownloadInfo,
    val obb: PubgDownloadInfo,
    val packageName: String,
    val displayName: String,
    val iconResource: String,
    val size: String
)

/**
 * Download information for APK or OBB files
 */
data class PubgDownloadInfo(
    val name: String,
    val url: String,
    val sha256: String,
    val size: Long = 0L
)

/**
 * Manifest loader utility
 */
object PubgManifestLoader {
    
    /**
     * Load manifest from assets and convert to PubgVariant list
     * Maintains compatibility with existing smart button system
     */
    fun loadFromAssets(context: android.content.Context): List<PubgVariant> {
        return try {
            android.util.Log.d("PubgManifestLoader", "Attempting to load pubg_manifest.json")

            val inputStream = context.assets.open("pubg_manifest.json")
            val jsonText = java.io.InputStreamReader(inputStream).readText()

            android.util.Log.d("PubgManifestLoader", "JSON loaded, length: ${jsonText.length}")

            val manifest = com.google.gson.Gson().fromJson(jsonText, PubgManifest::class.java)

            android.util.Log.d("PubgManifestLoader", "Manifest parsed, variants: ${manifest.variants.size}")

            val variants = convertToVariantList(manifest, context)

            android.util.Log.d("PubgManifestLoader", "Converted to ${variants.size} PubgVariant objects")

            variants
        } catch (e: Exception) {
            android.util.Log.e("PubgManifestLoader", "Failed to load manifest: ${e.message}", e)
            // Return empty list to trigger fallback in fragment
            emptyList()
        }
    }
    
    /**
     * Convert manifest to PubgVariant list with smart button states
     */
    private fun convertToVariantList(manifest: PubgManifest, context: android.content.Context): List<PubgVariant> {
        return manifest.variants.map { (key, info) ->
            val iconRes = getIconResource(key, context)

            PubgVariant(
                id = key.lowercase(),
                name = info.displayName,
                version = manifest.version,
                size = info.size,
                iconRes = iconRes,
                packageName = info.packageName,
                downloadUrl = info.apk.url,
                buttonState = PubgButtonState.DOWNLOAD, // Will be updated by version checker
                installedVersion = null,
                downloadProgress = 0
            )
        }
    }
    
    /**
     * Get icon resource ID from variant key
     */
    private fun getIconResource(variantKey: String, context: android.content.Context): Int {
        val resourceName = when (variantKey.uppercase()) {
            "GL", "GLOBAL" -> "ic_pubg_gl"
            "KR", "KOREA" -> "ic_pubg_kr"
            "TW", "TAIWAN" -> "ic_pubg_tw"
            "VNG", "VIETNAM" -> "ic_pubg_vng"
            "BGMI", "INDIA" -> "battleground_mobile_india"
            else -> "ic_launcher"
        }
        
        return try {
            val resourceId = context.resources.getIdentifier(
                resourceName, 
                "drawable", 
                context.packageName
            )
            if (resourceId != 0) resourceId else com.bearmod.loader.R.mipmap.ic_launcher
        } catch (e: Exception) {
            com.bearmod.loader.R.mipmap.ic_launcher
        }
    }
}
