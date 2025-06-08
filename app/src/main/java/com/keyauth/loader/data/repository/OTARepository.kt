package com.keyauth.loader.data.repository

import android.content.Context
import android.os.Environment
import com.keyauth.loader.config.KeyAuthConfig
import com.keyauth.loader.data.api.OTAApiService
import com.keyauth.loader.data.model.*
import com.keyauth.loader.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

/**
 * Repository for OTA update operations
 */
class OTARepository(
    private val context: Context,
    private val apiService: OTAApiService
) {
    
    private val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "ota_updates")
    private val obbDir = File(Environment.getExternalStorageDirectory(), "Android/obb/${context.packageName}")
    
    init {
        // Ensure download directory exists
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        
        // Ensure OBB directory exists
        if (!obbDir.exists()) {
            obbDir.mkdirs()
        }
    }
    
    /**
     * Check for available updates
     */
    suspend fun checkForUpdates(): NetworkResult<OTAResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getVersionInfo(KeyAuthConfig.OTA_VERSION_ENDPOINT)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error("Empty response from server")
                }
            } else {
                NetworkResult.Error("Network error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Failed to check for updates: ${e.message}")
        }
    }
    
    /**
     * Check if update is available
     */
    fun isUpdateAvailable(otaResponse: OTAResponse): Boolean {
        return otaResponse.version > KeyAuthConfig.CURRENT_VERSION ||
                (otaResponse.version == KeyAuthConfig.CURRENT_VERSION && otaResponse.build > KeyAuthConfig.CURRENT_BUILD)
    }
    
    /**
     * Download files for a specific variant
     */
    fun downloadVariant(variant: String, variantInfo: VariantInfo): Flow<Pair<DownloadProgress?, DownloadProgress?>> = flow {
        var apkProgress: DownloadProgress? = null
        var obbProgress: DownloadProgress? = null
        
        try {
            // Download APK
            emit(Pair(
                DownloadProgress(FileType.APK, 0, 0, 0f, DownloadStatus.PENDING),
                DownloadProgress(FileType.OBB, 0, 0, 0f, DownloadStatus.PENDING)
            ))
            
            // Download APK file
            val apkFile = File(downloadDir, "${variant}_${variantInfo.apk.name}")
            downloadFileWithProgress(variantInfo.apk.url, apkFile) { progress ->
                apkProgress = progress
                emit(Pair(apkProgress, obbProgress))
            }
            
            // Verify APK
            apkProgress = apkProgress?.copy(status = DownloadStatus.VERIFYING)
            emit(Pair(apkProgress, obbProgress))
            
            if (!verifyFileHash(apkFile, variantInfo.apk.sha256)) {
                apkProgress = apkProgress?.copy(status = DownloadStatus.VERIFICATION_FAILED)
                emit(Pair(apkProgress, obbProgress))
                throw Exception("APK verification failed")
            }
            
            apkProgress = apkProgress?.copy(status = DownloadStatus.VERIFIED)
            emit(Pair(apkProgress, obbProgress))
            
            // Download OBB file
            obbProgress = DownloadProgress(FileType.OBB, 0, 0, 0f, DownloadStatus.DOWNLOADING)
            emit(Pair(apkProgress, obbProgress))
            
            val obbFile = File(downloadDir, "${variant}_${variantInfo.obb.name}")
            downloadFileWithProgress(variantInfo.obb.url, obbFile) { progress ->
                obbProgress = progress
                emit(Pair(apkProgress, obbProgress))
            }
            
            // Verify OBB
            obbProgress = obbProgress?.copy(status = DownloadStatus.VERIFYING)
            emit(Pair(apkProgress, obbProgress))
            
            if (!verifyFileHash(obbFile, variantInfo.obb.sha256)) {
                obbProgress = obbProgress?.copy(status = DownloadStatus.VERIFICATION_FAILED)
                emit(Pair(apkProgress, obbProgress))
                throw Exception("OBB verification failed")
            }
            
            obbProgress = obbProgress?.copy(status = DownloadStatus.VERIFIED)
            emit(Pair(apkProgress, obbProgress))
            
        } catch (e: Exception) {
            val errorProgress = if (apkProgress?.status != DownloadStatus.VERIFIED) {
                apkProgress?.copy(status = DownloadStatus.FAILED)
            } else {
                obbProgress?.copy(status = DownloadStatus.FAILED)
            }
            
            if (apkProgress?.status != DownloadStatus.VERIFIED) {
                emit(Pair(errorProgress, obbProgress))
            } else {
                emit(Pair(apkProgress, errorProgress))
            }
            throw e
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Download a file with progress tracking
     */
    private suspend fun downloadFileWithProgress(
        url: String,
        destinationFile: File,
        onProgress: suspend (DownloadProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        val response = apiService.downloadFile(url)
        
        if (!response.isSuccessful) {
            throw Exception("Download failed: ${response.code()}")
        }
        
        val body = response.body() ?: throw Exception("Empty response body")
        val contentLength = body.contentLength()
        
        val fileType = if (destinationFile.name.endsWith(".apk")) FileType.APK else FileType.OBB
        
        body.byteStream().use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                val buffer = ByteArray(8192)
                var totalBytesRead = 0L
                var bytesRead: Int
                
                onProgress(DownloadProgress(fileType, 0, contentLength, 0f, DownloadStatus.DOWNLOADING))
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    
                    val progress = if (contentLength > 0) {
                        totalBytesRead.toFloat() / contentLength.toFloat()
                    } else {
                        0f
                    }
                    
                    onProgress(DownloadProgress(fileType, totalBytesRead, contentLength, progress, DownloadStatus.DOWNLOADING))
                }
                
                onProgress(DownloadProgress(fileType, totalBytesRead, contentLength, 1f, DownloadStatus.COMPLETED))
            }
        }
    }
    
    /**
     * Verify file hash
     */
    private suspend fun verifyFileHash(file: File, expectedHash: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            return@withContext actualHash.equals(expectedHash, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get downloaded file paths
     */
    fun getDownloadedFiles(variant: String, variantInfo: VariantInfo): Pair<File, File> {
        val apkFile = File(downloadDir, "${variant}_${variantInfo.apk.name}")
        val obbFile = File(downloadDir, "${variant}_${variantInfo.obb.name}")
        return Pair(apkFile, obbFile)
    }
    
    /**
     * Install OBB file to the correct location
     */
    suspend fun installOBBFile(obbFile: File, variant: String, variantInfo: VariantInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            val targetObbFile = File(obbDir, variantInfo.obb.name)
            
            // Copy OBB file to the correct location
            obbFile.copyTo(targetObbFile, overwrite = true)
            
            // Verify the copy was successful
            targetObbFile.exists() && targetObbFile.length() == obbFile.length()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clean up downloaded files
     */
    suspend fun cleanupDownloads() = withContext(Dispatchers.IO) {
        try {
            downloadDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}
