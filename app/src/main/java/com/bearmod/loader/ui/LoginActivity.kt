package com.bearmod.loader.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.bearmod.loader.R
import com.bearmod.loader.databinding.ActivityLoginBinding
import com.bearmod.loader.network.NetworkFactory
import com.bearmod.loader.ui.login.LoginViewModel
import com.bearmod.loader.data.model.SessionRestoreResult
import com.bearmod.loader.data.model.AuthFlowState
import com.bearmod.loader.utils.NetworkResult
import com.bearmod.loader.utils.SecurePreferences
import com.bearmod.loader.utils.PreferencesMigration
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var securePreferences: SecurePreferences
    
    private val viewModel: LoginViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = NetworkFactory.createKeyAuthRepository(this@LoginActivity)
                return LoginViewModel(repository) as T
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        securePreferences = SecurePreferences(this)

        // Migrate preferences from old implementation if needed
        PreferencesMigration.migrateIfNeeded(this, securePreferences)

        // Clear any corrupted session data that might cause "Session not found" errors
        clearCorruptedSessionData()

        setupUI()
        setupObservers()
        loadSavedPreferences()

        // Enhanced initialization with session restoration
        Log.d("LoginActivity", "🚀 Starting enhanced KeyAuth initialization with session restoration")

        // Check if we can attempt auto-login
        if (viewModel.canAutoLogin() && securePreferences.getAutoLogin()) {
            Log.d("LoginActivity", "🔄 Auto-login available, attempting session restoration...")
            viewModel.initializeWithSessionRestore()
        } else {
            Log.d("LoginActivity", "🔄 Standard initialization (no auto-login)")
            viewModel.initializeApp()
        }
    }
    
    private fun setupUI() {
        // Initially disable login button until initialization completes
        binding.btnLogin.isEnabled = false

        // Login button click
        binding.btnLogin.setOnClickListener {
            val licenseKey = binding.etLicenseKey.text.toString().trim()



            val validationError = viewModel.validateLicenseKey(licenseKey)

            if (validationError != null) {
                showError(validationError)
                return@setOnClickListener
            }

            // Double-check initialization before authentication
            if (!viewModel.isAppInitialized()) {
                showError("KeyAuth not initialized. Retrying initialization...")
                viewModel.initializeApp()
                return@setOnClickListener
            }

            viewModel.authenticateWithLicense(licenseKey)
        }
        
        // Paste icon click (TextInputLayout end icon)
        binding.tilLicenseKey.setEndIconOnClickListener {
            pasteFromClipboard()
        }
        
        // Remember key checkbox
        binding.cbRememberKey.setOnCheckedChangeListener { _, isChecked ->
            securePreferences.setRememberLicense(isChecked)
            if (!isChecked) {
                securePreferences.clearLicenseKey()
                binding.cbAutoLogin.isChecked = false
                securePreferences.setAutoLogin(false)
            }
        }
        
        // Auto login checkbox
        binding.cbAutoLogin.setOnCheckedChangeListener { _, isChecked ->
            securePreferences.setAutoLogin(isChecked)
            if (isChecked && !binding.cbRememberKey.isChecked) {
                binding.cbRememberKey.isChecked = true
                securePreferences.setRememberLicense(true)
            }
        }
    }
    
    private fun setupObservers() {
        // Observe initialization state
        viewModel.initState.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    showStatus("Initializing KeyAuth application...")
                    // Disable login button during initialization
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Initializing..."
                }
                is NetworkResult.Success -> {
                    showStatus("✅ KeyAuth initialized successfully")
                    // Enable login button after successful initialization
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "LOGIN"

                    // Check if auto login is enabled and we have a saved key
                    if (securePreferences.getAutoLogin()) {
                        val savedKey = securePreferences.getLicenseKey()
                        if (!savedKey.isNullOrEmpty()) {
                            binding.etLicenseKey.setText(savedKey)
                            showStatus("🔄 Auto-login with saved key...")
                            viewModel.authenticateWithLicense(savedKey)
                        }
                    }
                }
                is NetworkResult.Error -> {
                    showError("❌ KeyAuth initialization failed: ${result.message}")
                    // Enable retry button
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "RETRY INIT"

                    // Set click listener for retry
                    binding.btnLogin.setOnClickListener {
                        showStatus("Retrying KeyAuth initialization...")
                        viewModel.initializeApp()
                    }
                }
                null -> {
                    // Initial state - keep button disabled
                    binding.btnLogin.isEnabled = false
                }
            }
        }
        
        // Observe login state
        viewModel.loginState.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    showStatus("🔐 Authenticating with KeyAuth...")
                }
                is NetworkResult.Success -> {
                    showStatus("✅ Authentication successful!")

                    // Save license key if remember is checked
                    if (binding.cbRememberKey.isChecked) {
                        securePreferences.saveLicenseKey(binding.etLicenseKey.text.toString().trim())
                    }

                    // Navigate to main activity
                    navigateToMainActivity()
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Unknown error"
                    showError("❌ Authentication failed: $errorMessage")

                    // Clear corrupted session data for session-related errors
                    if (errorMessage.contains("session not found", ignoreCase = true) ||
                        errorMessage.contains("last code", ignoreCase = true) ||
                        errorMessage.contains("session expired", ignoreCase = true)) {
                        clearCorruptedSessionData()
                    }
                }
                null -> {
                    // Initial state
                }
            }
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Only disable button during loading if app is initialized
            if (viewModel.isAppInitialized()) {
                binding.btnLogin.isEnabled = !isLoading
                if (!isLoading && binding.btnLogin.text != "LOGIN") {
                    binding.btnLogin.text = "LOGIN"
                    // Restore normal login click listener
                    setupLoginClickListener()
                }
            }
        }

        // Observe session restoration state
        viewModel.sessionRestoreState.observe(this) { result ->
            when (result) {
                is SessionRestoreResult.Success -> {
                    showStatus("✅ Session restored successfully!")
                    navigateToMainActivity()
                }
                is SessionRestoreResult.NoStoredSession -> {
                    Log.d("LoginActivity", "ℹ️ No stored session, showing login form")
                    showStatus("Please enter your license key")
                }
                is SessionRestoreResult.SessionExpired -> {
                    showStatus("⏰ Session expired, please login again")
                    // Clear auto-login if session expired
                    binding.cbAutoLogin.isChecked = false
                    securePreferences.setAutoLogin(false)
                }
                is SessionRestoreResult.HWIDMismatch -> {
                    showError("⚠️ Device changed detected. Please re-authenticate.")
                    // Clear stored data for security
                    securePreferences.clearAuthenticationData()
                    binding.cbAutoLogin.isChecked = false
                    securePreferences.setAutoLogin(false)
                }
                is SessionRestoreResult.Failed -> {
                    showError("❌ Session restoration failed: ${result.error}")
                }
                null -> {
                    // Initial state
                }
            }
        }

        // Observe auth flow state for detailed feedback
        lifecycleScope.launch {
            viewModel.authFlowState.collect { state ->
                when (state) {
                    AuthFlowState.CHECKING_STORED_SESSION -> {
                        showStatus("🔍 Checking stored session...")
                    }
                    AuthFlowState.VALIDATING_HWID -> {
                        showStatus("🔐 Validating device identity...")
                    }
                    AuthFlowState.REFRESHING_TOKEN -> {
                        showStatus("🔄 Refreshing authentication...")
                    }
                    AuthFlowState.AUTHENTICATING_WITH_LICENSE -> {
                        showStatus("🔐 Authenticating with stored credentials...")
                    }
                    AuthFlowState.AUTHENTICATED -> {
                        showStatus("✅ Authentication successful!")
                    }
                    AuthFlowState.HWID_MISMATCH -> {
                        showError("⚠️ Device identity changed")
                    }
                    AuthFlowState.SESSION_EXPIRED -> {
                        showStatus("⏰ Session expired")
                    }
                    AuthFlowState.FAILED -> {
                        showError("❌ Authentication failed")
                    }
                    else -> {
                        // Other states handled elsewhere
                    }
                }
            }
        }
    }

    /**
     * Setup normal login click listener
     */
    private fun setupLoginClickListener() {
        binding.btnLogin.setOnClickListener {
            val licenseKey = binding.etLicenseKey.text.toString().trim()
            val validationError = viewModel.validateLicenseKey(licenseKey)

            if (validationError != null) {
                showError(validationError)
                return@setOnClickListener
            }

            // Double-check initialization before authentication
            if (!viewModel.isAppInitialized()) {
                showError("KeyAuth not initialized. Retrying initialization...")
                viewModel.initializeApp()
                return@setOnClickListener
            }

            viewModel.authenticateWithLicense(licenseKey)
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check initialization state when activity resumes
        if (!viewModel.isAppInitialized()) {
            showStatus("Re-initializing KeyAuth...")
            viewModel.initializeApp()
        } else {
            // Ensure UI is in correct state if already initialized
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "LOGIN"
            setupLoginClickListener()
        }
    }

    private fun loadSavedPreferences() {
        // Load saved preferences
        binding.cbRememberKey.isChecked = securePreferences.getRememberLicense()
        binding.cbAutoLogin.isChecked = securePreferences.getAutoLogin()
        
        // Load saved license key if remember is enabled
        if (securePreferences.getRememberLicense()) {
            val savedKey = securePreferences.getLicenseKey()
            if (!savedKey.isNullOrEmpty()) {
                binding.etLicenseKey.setText(savedKey)
            }
        }
    }
    
    private fun pasteFromClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip
        
        if (clipData != null && clipData.itemCount > 0) {
            val clipText = clipData.getItemAt(0).text?.toString()
            if (!clipText.isNullOrEmpty()) {
                binding.etLicenseKey.setText(clipText)
                Toast.makeText(this, "License key pasted", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showStatus(message: String) {
        // Determine snackbar type and duration based on message content
        when {
            message.contains("Initializing") || message.contains("初始化") -> {
                showInfoSnackbar(message, Snackbar.LENGTH_SHORT)
            }
            message.contains("successful") || message.contains("成功") -> {
                showSuccessSnackbar(message, Snackbar.LENGTH_LONG)
            }
            message.contains("Authenticating") || message.contains("认证") -> {
                showInfoSnackbar(message, Snackbar.LENGTH_SHORT)
            }
            else -> {
                showInfoSnackbar(message, Snackbar.LENGTH_SHORT)
            }
        }
    }

    private fun showError(message: String) {
        showErrorSnackbar(message, Snackbar.LENGTH_LONG)
    }

    private fun showSuccess(message: String) {
        showSuccessSnackbar(message, Snackbar.LENGTH_LONG)
    }

    // Material Design 3 Snackbar helper methods
    private fun showSuccessSnackbar(message: String, duration: Int) {
        val snackbar = Snackbar.make(binding.root, message, duration)
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.success_green))
        snackbar.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        snackbar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
        snackbar.show()
    }

    private fun showErrorSnackbar(message: String, duration: Int) {
        val snackbar = Snackbar.make(binding.root, message, duration)
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.error_red))
        snackbar.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        snackbar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
        snackbar.show()
    }

    private fun showInfoSnackbar(message: String, duration: Int) {
        val snackbar = Snackbar.make(binding.root, message, duration)
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.md3_primary))
        snackbar.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        snackbar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
        snackbar.show()
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Clear corrupted session data that might cause "Session not found" errors
     * This forces a fresh authentication instead of trying to restore a broken session
     */
    private fun clearCorruptedSessionData() {
        try {
            // Check if session token exists but might be corrupted
            val sessionToken = securePreferences.getSessionToken()
            if (!sessionToken.isNullOrEmpty()) {
                // If we have a session token but it's causing "Session not found" errors,
                // it might be corrupted or expired on the server side
                Log.d("LoginActivity", "🧹 Clearing potentially corrupted session data")

                // Clear session-related data but keep user preferences
                securePreferences.clearSessionToken()

                // Also clear device registration if it might be causing issues
                if (!securePreferences.isDeviceRegistered()) {
                    securePreferences.clearDeviceRegistration()
                }

                Log.d("LoginActivity", "✅ Session data cleared, will perform fresh authentication")
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "❌ Error clearing corrupted session data", e)
        }
    }

}
