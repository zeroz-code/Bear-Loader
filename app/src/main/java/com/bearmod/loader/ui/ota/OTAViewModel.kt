package com.bearmod.loader.ui.ota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bearmod.loader.config.KeyAuthConfig
import com.bearmod.loader.data.model.*
import com.bearmod.loader.data.repository.OTARepository
import com.bearmod.loader.utils.APKInstaller
import com.bearmod.loader.utils.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for OTA update functionality
 */
class OTAViewModel(
    private val otaRepository: OTARepository,
    private val apkInstaller: APKInstaller
) : ViewModel() {
    
    private val _updateState = MutableStateFlow<OTAUpdateState>(OTAUpdateState.Idle)
    val updateState: StateFlow<OTAUpdateState> = _updateState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var currentOTAResponse: OTAResponse? = null
    private var selectedVariant: String? = null
    
    /**
     * Check for available updates
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            _isLoading.value = true
            _updateState.value = OTAUpdateState.CheckingForUpdates
            
            try {
                when (val result = otaRepository.checkForUpdates()) {
                    is NetworkResult.Success -> {
                        currentOTAResponse = result.data
                        
                        if (otaRepository.isUpdateAvailable(result.data)) {
                            _updateState.value = OTAUpdateState.UpdateAvailable(result.data)
                        } else {
                            _updateState.value = OTAUpdateState.NoUpdateAvailable
                        }
                    }
                    is NetworkResult.Error -> {
                        _updateState.value = OTAUpdateState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        // Already handled above
                    }
                }
            } catch (e: Exception) {
                _updateState.value = OTAUpdateState.Error("Failed to check for updates: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Proceed to variant selection
     */
    fun proceedToVariantSelection() {
        currentOTAResponse?.let { response ->
            _updateState.value = OTAUpdateState.VariantSelection(response)
        }
    }
    
    /**
     * Select a variant and start download
     */
    fun selectVariant(variant: String) {
        val otaResponse = currentOTAResponse ?: return
        val variantInfo = otaResponse.variants[variant] ?: return
        
        selectedVariant = variant
        startDownload(variant, variantInfo)
    }
    
    /**
     * Start downloading the selected variant
     */
    private fun startDownload(variant: String, variantInfo: VariantInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                otaRepository.downloadVariant(variant, variantInfo)
                    .collect { (apkProgress, obbProgress) ->
                        _updateState.value = OTAUpdateState.Downloading(apkProgress, obbProgress)
                        
                        // Check if both downloads are completed and verified
                        if (apkProgress?.status == DownloadStatus.VERIFIED && 
                            obbProgress?.status == DownloadStatus.VERIFIED) {
                            
                            val (apkFile, obbFile) = otaRepository.getDownloadedFiles(variant, variantInfo)
                            _updateState.value = OTAUpdateState.DownloadCompleted(
                                apkFile.absolutePath,
                                obbFile.absolutePath
                            )
                            _isLoading.value = false
                        }
                    }
            } catch (e: Exception) {
                _updateState.value = OTAUpdateState.Error("Download failed: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Install the downloaded files
     */
    fun installDownloadedFiles() {
        val variant = selectedVariant ?: return
        val otaResponse = currentOTAResponse ?: return
        val variantInfo = otaResponse.variants[variant] ?: return
        
        viewModelScope.launch {
            _updateState.value = OTAUpdateState.Installing("Installing OBB file...")
            
            try {
                val (apkFile, obbFile) = otaRepository.getDownloadedFiles(variant, variantInfo)
                
                // First install OBB file
                val obbInstalled = otaRepository.installOBBFile(obbFile, variant, variantInfo)
                
                if (!obbInstalled) {
                    _updateState.value = OTAUpdateState.InstallationCompleted(
                        InstallationResult(false, "Failed to install OBB file")
                    )
                    return@launch
                }
                
                _updateState.value = OTAUpdateState.Installing("Installing APK file...")
                
                // Then install APK file
                val apkInstalled = apkInstaller.installAPK(apkFile)
                
                if (apkInstalled) {
                    _updateState.value = OTAUpdateState.InstallationCompleted(
                        InstallationResult(true, "Installation completed successfully")
                    )
                    
                    // Clean up downloaded files after successful installation
                    otaRepository.cleanupDownloads()
                } else {
                    _updateState.value = OTAUpdateState.InstallationCompleted(
                        InstallationResult(false, "Failed to install APK file")
                    )
                }
                
            } catch (e: Exception) {
                _updateState.value = OTAUpdateState.InstallationCompleted(
                    InstallationResult(false, "Installation failed: ${e.message}")
                )
            }
        }
    }
    
    /**
     * Retry the current operation
     */
    fun retry() {
        when (val currentState = _updateState.value) {
            is OTAUpdateState.Error -> {
                checkForUpdates()
            }
            is OTAUpdateState.DownloadCompleted -> {
                installDownloadedFiles()
            }
            else -> {
                checkForUpdates()
            }
        }
    }
    
    /**
     * Reset to idle state
     */
    fun reset() {
        _updateState.value = OTAUpdateState.Idle
        _isLoading.value = false
        currentOTAResponse = null
        selectedVariant = null
    }
    
    /**
     * Get available variants as UI items
     */
    fun getAvailableVariants(): List<VariantItem> {
        val otaResponse = currentOTAResponse ?: return emptyList()
        
        return KeyAuthConfig.AVAILABLE_VARIANTS.mapNotNull { variant ->
            val variantInfo = otaResponse.variants[variant]
            if (variantInfo != null) {
                VariantItem(
                    id = variant,
                    displayName = getVariantDisplayName(variant),
                    description = getVariantDescription(variant),
                    isAvailable = true,
                    variantInfo = variantInfo
                )
            } else {
                null
            }
        }
    }
    
    /**
     * Get display name for variant
     */
    private fun getVariantDisplayName(variant: String): String {
        return when (variant) {
            "GL" -> "Global"
            "KR" -> "Korea"
            "TW" -> "Taiwan"
            "VNG" -> "Vietnam"
            "BGMI" -> "Battlegrounds Mobile India"
            else -> variant
        }
    }
    
    /**
     * Get description for variant
     */
    private fun getVariantDescription(variant: String): String {
        return when (variant) {
            "GL" -> "Global version with worldwide support"
            "KR" -> "Korean version optimized for Korea region"
            "TW" -> "Taiwan version with traditional Chinese support"
            "VNG" -> "Vietnam version with local optimizations"
            "BGMI" -> "Battlegrounds Mobile India - exclusive version for Indian players"
            else -> "Regional variant: $variant"
        }
    }
    
    /**
     * Get current version info
     */
    fun getCurrentVersionInfo(): String {
        return "Current Version: ${KeyAuthConfig.CURRENT_VERSION} (Build ${KeyAuthConfig.CURRENT_BUILD})"
    }
    
    /**
     * Get available version info
     */
    fun getAvailableVersionInfo(): String {
        val otaResponse = currentOTAResponse ?: return "Unknown"
        return "Available Version: ${otaResponse.version} (Build ${otaResponse.build})"
    }
}
