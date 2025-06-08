package com.keyauth.loader.ui.ota

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.keyauth.loader.data.model.DownloadStatus
import com.keyauth.loader.data.model.FileType
import com.keyauth.loader.data.model.OTAUpdateState
import com.keyauth.loader.databinding.ActivityDownloadBinding
import com.keyauth.loader.network.NetworkFactory
import com.keyauth.loader.utils.APKInstaller
import kotlinx.coroutines.launch

/**
 * Activity for showing download progress
 */
class DownloadActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDownloadBinding
    
    private val viewModel: OTAViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val otaRepository = NetworkFactory.createOTARepository(this@DownloadActivity)
                val apkInstaller = APKInstaller(this@DownloadActivity)
                return OTAViewModel(otaRepository, apkInstaller) as T
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackPressedCallback()
        setupUI()
        setupObservers()
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Only allow back if not downloading or installing
                val currentState = viewModel.updateState.value
                if (currentState !is OTAUpdateState.Downloading && currentState !is OTAUpdateState.Installing) {
                    finish()
                }
                // If downloading or installing, do nothing (block back navigation)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
    
    private fun setupUI() {
        // Back button (disabled during download)
        binding.toolbar.setNavigationOnClickListener {
            // Only allow back if not downloading
            val currentState = viewModel.updateState.value
            if (currentState !is OTAUpdateState.Downloading) {
                finish()
            }
        }
        
        // Install button
        binding.btnInstall.setOnClickListener {
            viewModel.installDownloadedFiles()
        }
        
        // Retry button
        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
        
        // Cancel button
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun setupObservers() {
        // Observe update state
        lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                updateUI(state)
            }
        }
        
        // Observe loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Loading state is handled by update state
            }
        }
    }
    
    private fun updateUI(state: OTAUpdateState) {
        when (state) {
            is OTAUpdateState.Downloading -> {
                showDownloadingState(state)
            }
            is OTAUpdateState.DownloadCompleted -> {
                showDownloadCompletedState(state)
            }
            is OTAUpdateState.Installing -> {
                showInstallingState(state)
            }
            is OTAUpdateState.InstallationCompleted -> {
                showInstallationCompletedState(state)
            }
            is OTAUpdateState.Error -> {
                showErrorState(state.message)
            }
            else -> {
                // Handle other states if needed
            }
        }
    }
    
    private fun showDownloadingState(state: OTAUpdateState.Downloading) {
        binding.apply {
            // Update title
            tvTitle.text = "Downloading Update"
            tvDescription.text = "Please wait while the update files are being downloaded..."
            
            // Show progress sections
            layoutApkProgress.visibility = View.VISIBLE
            layoutObbProgress.visibility = View.VISIBLE
            layoutOverallProgress.visibility = View.VISIBLE
            
            // Update APK progress
            state.apkProgress?.let { progress ->
                updateProgressSection(
                    tvApkStatus,
                    progressApk,
                    tvApkProgress,
                    progress,
                    "APK File"
                )
            }
            
            // Update OBB progress
            state.obbProgress?.let { progress ->
                updateProgressSection(
                    tvObbStatus,
                    progressObb,
                    tvObbProgress,
                    progress,
                    "OBB File"
                )
            }
            
            // Calculate overall progress
            val apkProgress = state.apkProgress?.progress ?: 0f
            val obbProgress = state.obbProgress?.progress ?: 0f
            val overallProgress = (apkProgress + obbProgress) / 2f
            
            progressOverall.progress = (overallProgress * 100).toInt()
            tvOverallProgress.text = "${(overallProgress * 100).toInt()}%"
            
            // Hide action buttons during download
            layoutActions.visibility = View.GONE
            
            // Disable back navigation during download
            toolbar.navigationIcon?.alpha = 128
        }
    }
    
    private fun updateProgressSection(
        statusTextView: android.widget.TextView,
        progressBar: com.google.android.material.progressindicator.LinearProgressIndicator,
        progressTextView: android.widget.TextView,
        progress: com.keyauth.loader.data.model.DownloadProgress,
        fileType: String
    ) {
        // Update status text
        statusTextView.text = when (progress.status) {
            DownloadStatus.PENDING -> "$fileType - Pending"
            DownloadStatus.DOWNLOADING -> "$fileType - Downloading"
            DownloadStatus.COMPLETED -> "$fileType - Downloaded"
            DownloadStatus.VERIFYING -> "$fileType - Verifying"
            DownloadStatus.VERIFIED -> "$fileType - Verified"
            DownloadStatus.FAILED -> "$fileType - Failed"
            DownloadStatus.VERIFICATION_FAILED -> "$fileType - Verification Failed"
        }
        
        // Update progress bar
        progressBar.progress = progress.progressPercentage
        progressTextView.text = "${progress.progressPercentage}%"
        
        // Update colors based on status
        val color = when (progress.status) {
            DownloadStatus.VERIFIED -> android.graphics.Color.GREEN
            DownloadStatus.FAILED, DownloadStatus.VERIFICATION_FAILED -> android.graphics.Color.RED
            else -> null
        }
        
        color?.let {
            statusTextView.setTextColor(it)
        }
    }
    
    private fun showDownloadCompletedState(state: OTAUpdateState.DownloadCompleted) {
        binding.apply {
            tvTitle.text = "Download Completed"
            tvDescription.text = "All files have been downloaded and verified successfully. Ready to install."
            
            // Hide progress sections
            layoutApkProgress.visibility = View.GONE
            layoutObbProgress.visibility = View.GONE
            layoutOverallProgress.visibility = View.GONE
            
            // Show action buttons
            layoutActions.visibility = View.VISIBLE
            btnInstall.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
            btnRetry.visibility = View.GONE
            
            // Enable back navigation
            toolbar.navigationIcon?.alpha = 255
        }
    }
    
    private fun showInstallingState(state: OTAUpdateState.Installing) {
        binding.apply {
            tvTitle.text = "Installing Update"
            tvDescription.text = state.message
            
            // Hide progress sections
            layoutApkProgress.visibility = View.GONE
            layoutObbProgress.visibility = View.GONE
            layoutOverallProgress.visibility = View.GONE
            
            // Hide action buttons during installation
            layoutActions.visibility = View.GONE
            
            // Disable back navigation during installation
            toolbar.navigationIcon?.alpha = 128
        }
    }
    
    private fun showInstallationCompletedState(state: OTAUpdateState.InstallationCompleted) {
        binding.apply {
            if (state.result.success) {
                tvTitle.text = "Installation Completed"
                tvDescription.text = state.result.message
                
                // Show success actions
                layoutActions.visibility = View.VISIBLE
                btnInstall.visibility = View.GONE
                btnCancel.text = "Close"
                btnCancel.visibility = View.VISIBLE
                btnRetry.visibility = View.GONE
            } else {
                tvTitle.text = "Installation Failed"
                tvDescription.text = state.result.message
                
                // Show retry actions
                layoutActions.visibility = View.VISIBLE
                btnInstall.visibility = View.GONE
                btnCancel.visibility = View.VISIBLE
                btnRetry.visibility = View.VISIBLE
            }
            
            // Enable back navigation
            toolbar.navigationIcon?.alpha = 255
        }
    }
    
    private fun showErrorState(message: String) {
        binding.apply {
            tvTitle.text = "Download Failed"
            tvDescription.text = message
            
            // Hide progress sections
            layoutApkProgress.visibility = View.GONE
            layoutObbProgress.visibility = View.GONE
            layoutOverallProgress.visibility = View.GONE
            
            // Show retry actions
            layoutActions.visibility = View.VISIBLE
            btnInstall.visibility = View.GONE
            btnCancel.visibility = View.VISIBLE
            btnRetry.visibility = View.VISIBLE
            
            // Enable back navigation
            toolbar.navigationIcon?.alpha = 255
        }
    }
    

}
