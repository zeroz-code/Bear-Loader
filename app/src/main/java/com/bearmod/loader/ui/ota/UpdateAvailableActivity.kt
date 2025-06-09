package com.bearmod.loader.ui.ota

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bearmod.loader.R
import com.bearmod.loader.data.model.OTAUpdateState
import com.bearmod.loader.databinding.ActivityUpdateAvailableBinding
import com.bearmod.loader.network.NetworkFactory
import com.bearmod.loader.utils.APKInstaller
import com.bearmod.loader.utils.PermissionManager
import kotlinx.coroutines.launch

/**
 * Activity to show update available screen
 */
class UpdateAvailableActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUpdateAvailableBinding
    private lateinit var permissionManager: PermissionManager

    // Activity result launcher for install permission
    private val installPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        permissionManager.handleInstallPermissionResult(
            PermissionManager.REQUEST_INSTALL_PACKAGES,
            onGranted = {
                viewModel.proceedToVariantSelection()
            },
            onDenied = {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Install permission is required to automatically install updates.")
                    .setPositiveButton("Settings") { _, _ ->
                        permissionManager.openAppSettings(this)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
    }
    
    private val viewModel: OTAViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val otaRepository = NetworkFactory.createOTARepository(this@UpdateAvailableActivity)
                val apkInstaller = APKInstaller(this@UpdateAvailableActivity)
                return OTAViewModel(otaRepository, apkInstaller) as T
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateAvailableBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        permissionManager = PermissionManager(this)
        
        setupUI()
        setupObservers()
        
        // Check for updates when activity starts
        viewModel.checkForUpdates()
    }
    
    private fun setupUI() {
        // Update button click
        binding.btnUpdate.setOnClickListener {
            if (permissionManager.hasAllRequiredPermissions()) {
                viewModel.proceedToVariantSelection()
            } else {
                showPermissionDialog()
            }
        }
        
        // Later button click
        binding.btnLater.setOnClickListener {
            finish()
        }
        
        // Retry button click
        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
        
        // Back button
        binding.toolbar.setNavigationOnClickListener {
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
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun updateUI(state: OTAUpdateState) {
        when (state) {
            is OTAUpdateState.Idle -> {
                showIdleState()
            }
            is OTAUpdateState.CheckingForUpdates -> {
                showCheckingState()
            }
            is OTAUpdateState.UpdateAvailable -> {
                showUpdateAvailableState(state)
            }
            is OTAUpdateState.NoUpdateAvailable -> {
                showNoUpdateState()
            }
            is OTAUpdateState.VariantSelection -> {
                navigateToVariantSelection()
            }
            is OTAUpdateState.Error -> {
                showErrorState(state.message)
            }
            else -> {
                // Other states handled in different activities
            }
        }
    }
    
    private fun showIdleState() {
        binding.apply {
            tvTitle.text = "Checking for Updates"
            tvDescription.text = "Please wait while we check for available updates..."
            tvCurrentVersion.text = viewModel.getCurrentVersionInfo()
            tvAvailableVersion.visibility = View.GONE
            btnUpdate.visibility = View.GONE
            btnLater.visibility = View.GONE
            btnRetry.visibility = View.GONE
        }
    }
    
    private fun showCheckingState() {
        binding.apply {
            tvTitle.text = "Checking for Updates"
            tvDescription.text = "Connecting to update server..."
            tvCurrentVersion.text = viewModel.getCurrentVersionInfo()
            tvAvailableVersion.visibility = View.GONE
            btnUpdate.visibility = View.GONE
            btnLater.visibility = View.GONE
            btnRetry.visibility = View.GONE
        }
    }
    
    private fun showUpdateAvailableState(state: OTAUpdateState.UpdateAvailable) {
        binding.apply {
            tvTitle.text = "Update Available"
            tvDescription.text = "A new version of BearMod is available for download. This update includes new features, improvements, and bug fixes."
            tvCurrentVersion.text = viewModel.getCurrentVersionInfo()
            tvAvailableVersion.text = viewModel.getAvailableVersionInfo()
            tvAvailableVersion.visibility = View.VISIBLE
            btnUpdate.visibility = View.VISIBLE
            btnLater.visibility = View.VISIBLE
            btnRetry.visibility = View.GONE
        }
    }
    
    private fun showNoUpdateState() {
        binding.apply {
            tvTitle.text = "No Updates Available"
            tvDescription.text = "You are already using the latest version of BearMod."
            tvCurrentVersion.text = viewModel.getCurrentVersionInfo()
            tvAvailableVersion.visibility = View.GONE
            btnUpdate.visibility = View.GONE
            btnLater.text = "Close"
            btnLater.visibility = View.VISIBLE
            btnRetry.visibility = View.GONE
        }
    }
    
    private fun showErrorState(message: String) {
        binding.apply {
            tvTitle.text = "Update Check Failed"
            tvDescription.text = "Failed to check for updates: $message"
            tvCurrentVersion.text = viewModel.getCurrentVersionInfo()
            tvAvailableVersion.visibility = View.GONE
            btnUpdate.visibility = View.GONE
            btnLater.visibility = View.VISIBLE
            btnRetry.visibility = View.VISIBLE
        }
    }
    
    private fun showPermissionDialog() {
        val explanation = permissionManager.getPermissionExplanation()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage(explanation)
            .setPositiveButton("Grant Permissions") { _, _ ->
                permissionManager.requestAllPermissions(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun navigateToVariantSelection() {
        val intent = Intent(this, VariantSelectionActivity::class.java)
        startActivity(intent)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        permissionManager.handlePermissionResult(
            requestCode,
            permissions,
            grantResults,
            onAllGranted = {
                viewModel.proceedToVariantSelection()
            },
            onDenied = { deniedPermissions ->
                val message = "The following permissions are required: ${deniedPermissions.joinToString(", ")}"
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permissions Denied")
                    .setMessage(message)
                    .setPositiveButton("Settings") { _, _ ->
                        permissionManager.openAppSettings(this)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
    }
    

}
