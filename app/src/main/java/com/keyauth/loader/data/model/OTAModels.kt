package com.keyauth.loader.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data models for OTA update system
 */

/**
 * Main OTA response containing version and variants information
 */
data class OTAResponse(
    @SerializedName("version")
    val version: Int,
    
    @SerializedName("build")
    val build: Int,
    
    @SerializedName("variants")
    val variants: Map<String, VariantInfo>
)

/**
 * Information about a specific variant
 */
data class VariantInfo(
    @SerializedName("apk")
    val apk: FileInfo,
    
    @SerializedName("obb")
    val obb: FileInfo
)

/**
 * Information about a downloadable file (APK or OBB)
 */
data class FileInfo(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("sha256")
    val sha256: String,
    
    @SerializedName("size")
    val size: Long? = null
)

/**
 * Download progress information
 */
data class DownloadProgress(
    val fileType: FileType,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val progress: Float,
    val status: DownloadStatus
) {
    val progressPercentage: Int
        get() = (progress * 100).toInt()
}

/**
 * Type of file being downloaded
 */
enum class FileType {
    APK,
    OBB
}

/**
 * Download status
 */
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    VERIFYING,
    VERIFIED,
    VERIFICATION_FAILED
}

/**
 * Variant selection item for UI
 */
data class VariantItem(
    val id: String,
    val displayName: String,
    val description: String,
    val iconRes: Int? = null,
    val isAvailable: Boolean = true,
    val variantInfo: VariantInfo? = null
)

/**
 * Installation result
 */
data class InstallationResult(
    val success: Boolean,
    val message: String,
    val errorCode: Int? = null
)

/**
 * OTA update state
 */
sealed class OTAUpdateState {
    object Idle : OTAUpdateState()
    object CheckingForUpdates : OTAUpdateState()
    data class UpdateAvailable(val otaResponse: OTAResponse) : OTAUpdateState()
    object NoUpdateAvailable : OTAUpdateState()
    data class VariantSelection(val otaResponse: OTAResponse) : OTAUpdateState()
    data class Downloading(val apkProgress: DownloadProgress?, val obbProgress: DownloadProgress?) : OTAUpdateState()
    data class DownloadCompleted(val apkPath: String, val obbPath: String) : OTAUpdateState()
    data class Installing(val message: String) : OTAUpdateState()
    data class InstallationCompleted(val result: InstallationResult) : OTAUpdateState()
    data class Error(val message: String, val throwable: Throwable? = null) : OTAUpdateState()
}
