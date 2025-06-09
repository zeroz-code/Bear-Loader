package com.bearmod.loader.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.bearmod.loader.R
import com.bearmod.loader.data.api.KeyAuthApiService
import com.bearmod.loader.data.repository.KeyAuthRepository
import com.bearmod.loader.ui.LoginActivity
import com.bearmod.loader.ui.MainActivity
import com.bearmod.loader.utils.LanguageManager
import com.bearmod.loader.utils.SessionManager
import com.bearmod.loader.utils.SecurePreferences
import com.bearmod.loader.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Settings Fragment with comprehensive user information,
 * license details, and relocated app action buttons
 */
class EnhancedSettingsFragment : Fragment() {

    // UI Components
    private lateinit var tvCurrentLanguage: TextView
    private lateinit var tvLicenseKey: TextView
    private lateinit var tvLicenseExpiry: TextView
    private lateinit var tvTimeRemaining: TextView
    private lateinit var tvLastValidation: TextView
    private lateinit var tvLicenseStatus: TextView

    // Section headers
    private lateinit var tvLicenseInfoHeader: TextView
    private lateinit var tvLanguageSettingsHeader: TextView
    private lateinit var tvLanguageDescription: TextView

    // Utilities
    private lateinit var languageManager: LanguageManager
    private lateinit var sessionManager: SessionManager
    private lateinit var securePreferences: SecurePreferences
    private lateinit var authViewModel: AuthViewModel

    // Update handler for countdown
    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageManager = LanguageManager(requireContext())
        sessionManager = SessionManager(requireContext())
        securePreferences = SecurePreferences(requireContext())
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        initViews(view)
        setupClickListeners()
        setupObservers()
        updateLanguage()
        loadUserInformation()
        startPeriodicUpdates()

        return view
    }

    private fun initViews(view: View) {
        // Section headers
        tvLicenseInfoHeader = view.findViewById(R.id.tvLicenseInfoHeader)
        tvLanguageSettingsHeader = view.findViewById(R.id.tvLanguageSettingsHeader)
        tvLanguageDescription = view.findViewById(R.id.tvLanguageDescription)

        // Language and basic settings
        tvCurrentLanguage = view.findViewById(R.id.tvCurrentLanguage)

        // License information displays
        tvLicenseKey = view.findViewById(R.id.tvLicenseKey)
        tvLicenseExpiry = view.findViewById(R.id.tvLicenseExpiry)
        tvTimeRemaining = view.findViewById(R.id.tvTimeRemaining)
        tvLastValidation = view.findViewById(R.id.tvLastValidation)
        tvLicenseStatus = view.findViewById(R.id.tvLicenseStatus)
    }

    private fun setupClickListeners() {
        try {
            // Language selection card click - Safe view access with null checks
            val cardLanguageSelection = view?.findViewById<View>(R.id.cardLanguageSelection)
            if (cardLanguageSelection != null) {
                cardLanguageSelection.setOnClickListener {
                    Log.d("EnhancedSettingsFragment", "Language card clicked")
                    if (isAdded && !isDetached) {
                        showLanguageDialog()
                    }
                }
                Log.d("EnhancedSettingsFragment", "Language card click listener set successfully")
            } else {
                Log.w("EnhancedSettingsFragment", "Language card not found in layout")
            }
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error setting up click listeners", e)
        }
    }

    private fun setupObservers() {
        // Note: Authentication state observation would require KeyAuthRepository instance
        // For now, we'll use stored session data to determine authentication status
    }

    private fun loadUserInformation() {
        // Load license information
        updateLicenseInformation()
    }



    private fun updateLicenseInformation() {
        try {
            // Check if fragment is still attached and views are available
            if (!isAdded || isDetached || view == null) {
                Log.w("EnhancedSettingsFragment", "Fragment not attached, skipping license info update")
                return
            }

            // Try to get license key from multiple sources for better reliability
            val licenseKey = securePreferences.getLicenseKey() ?: securePreferences.getBoundLicenseKey()
            val sessionToken = securePreferences.getSessionToken()
            val tokenExpiry = securePreferences.getTokenExpiryTime()

            Log.d("EnhancedSettingsFragment", "License info update - Key: ${if (licenseKey.isNullOrEmpty()) "null/empty" else "available"}, Token: ${if (sessionToken.isNullOrEmpty()) "null/empty" else "available"}, Expiry: $tokenExpiry")

            // Update license key (masked for security) with null checks
            if (::tvLicenseKey.isInitialized) {
                if (!licenseKey.isNullOrEmpty()) {
                    val maskedKey = if (licenseKey.length >= 8) {
                        licenseKey.take(4) + "-****-****-" + licenseKey.takeLast(4)
                    } else {
                        "****-****-****-****" // Fallback for short keys
                    }
                    val keyText = if (languageManager.isChineseEnabled()) {
                        "密钥: $maskedKey"
                    } else {
                        "Key: $maskedKey"
                    }
                    tvLicenseKey.text = keyText
                } else {
                    val noKeyText = if (languageManager.isChineseEnabled()) {
                        "密钥: 不可用"
                    } else {
                        "Key: Not Available"
                    }
                    tvLicenseKey.text = noKeyText
                }
            }

            // Update license status based on actual session validity with null checks
            if (::tvLicenseStatus.isInitialized) {
                val isValid = !sessionToken.isNullOrEmpty() && securePreferences.isSessionTokenValid()
                if (isValid) {
                    tvLicenseStatus.text = if (languageManager.isChineseEnabled()) "有效" else "Valid"
                    tvLicenseStatus.setTextColor(resources.getColor(R.color.success_green, null))
                } else {
                    tvLicenseStatus.text = if (languageManager.isChineseEnabled()) "无效" else "Invalid"
                    tvLicenseStatus.setTextColor(resources.getColor(R.color.error_red, null))
                }
            }

            // Update expiry information with real token data
            updateExpiryInformation(tokenExpiry)

            // Update last validation with real timestamp with null checks
            if (::tvLastValidation.isInitialized) {
                val lastValidation = if (languageManager.isChineseEnabled()) {
                    "最后验证: ${getCurrentTimestamp()}"
                } else {
                    "Last Validation: ${getCurrentTimestamp()}"
                }
                tvLastValidation.text = lastValidation
            }
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error updating license information", e)
        }
    }

    private fun updateExpiryInformation(expiryTime: Long) {
        try {
            // Check if fragment is still attached and views are available
            if (!isAdded || isDetached || view == null) {
                Log.w("EnhancedSettingsFragment", "Fragment not attached, skipping expiry info update")
                return
            }

            Log.d("EnhancedSettingsFragment", "Updating expiry info - Expiry time: $expiryTime, Current time: ${System.currentTimeMillis()}")

            if (expiryTime > 0) {
                val currentTime = System.currentTimeMillis()
                val timeRemaining = expiryTime - currentTime

                Log.d("EnhancedSettingsFragment", "Time remaining: $timeRemaining ms")

                if (timeRemaining > 0) {
                    // Format expiry date with proper locale
                    val dateFormat = if (languageManager.isChineseEnabled()) {
                        SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINESE)
                    } else {
                        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.ENGLISH)
                    }
                    val expiryDate = dateFormat.format(Date(expiryTime))

                    val expiryText = if (languageManager.isChineseEnabled()) {
                        "到期时间: $expiryDate"
                    } else {
                        "Expires: $expiryDate"
                    }

                    // Update expiry text with null check
                    if (::tvLicenseExpiry.isInitialized) {
                        tvLicenseExpiry.text = expiryText
                    }

                    // Format time remaining with real data
                    val remainingText = formatTimeRemaining(timeRemaining)

                    // Update time remaining with null check
                    if (::tvTimeRemaining.isInitialized) {
                        tvTimeRemaining.text = remainingText

                        // Color coding based on time remaining
                        val color = when {
                            timeRemaining < 60 * 60 * 1000 -> R.color.error_red // Less than 1 hour
                            timeRemaining < 24 * 60 * 60 * 1000 -> R.color.warning_yellow // Less than 1 day
                            else -> R.color.accent_orange // More than 1 day
                        }
                        tvTimeRemaining.setTextColor(resources.getColor(color, null))
                    }
                } else {
                    // Expired - update with null checks
                    if (::tvLicenseExpiry.isInitialized) {
                        tvLicenseExpiry.text = if (languageManager.isChineseEnabled()) {
                            "到期时间: 已过期"
                        } else {
                            "Expires: Expired"
                        }
                    }

                    if (::tvTimeRemaining.isInitialized) {
                        tvTimeRemaining.text = if (languageManager.isChineseEnabled()) {
                            "剩余时间: 已过期"
                        } else {
                            "Time Remaining: Expired"
                        }
                        tvTimeRemaining.setTextColor(resources.getColor(R.color.error_red, null))
                    }
                }
            } else {
                // No expiry time available - update with null checks
                Log.w("EnhancedSettingsFragment", "No expiry time available - session may not be properly stored")

                if (::tvLicenseExpiry.isInitialized) {
                    tvLicenseExpiry.text = if (languageManager.isChineseEnabled()) {
                        "到期时间: 未设置"
                    } else {
                        "Expires: Not Set"
                    }
                }

                if (::tvTimeRemaining.isInitialized) {
                    tvTimeRemaining.text = if (languageManager.isChineseEnabled()) {
                        "剩余时间: 未知"
                    } else {
                        "Time Remaining: Unknown"
                    }
                    tvTimeRemaining.setTextColor(resources.getColor(R.color.text_secondary, null))
                }
            }
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error updating expiry information", e)
        }
    }

    private fun formatTimeRemaining(timeMs: Long): String {
        val days = timeMs / (24 * 60 * 60 * 1000)
        val hours = (timeMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (timeMs % (60 * 60 * 1000)) / (60 * 1000)
        
        return if (languageManager.isChineseEnabled()) {
            when {
                days > 0 -> "剩余时间: ${days}天 ${hours}小时"
                hours > 0 -> "剩余时间: ${hours}小时 ${minutes}分钟"
                else -> "剩余时间: ${minutes}分钟"
            }
        } else {
            when {
                days > 0 -> "Time Remaining: ${days}d ${hours}h"
                hours > 0 -> "Time Remaining: ${hours}h ${minutes}m"
                else -> "Time Remaining: ${minutes}m"
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }



    private fun startPeriodicUpdates() {
        try {
            // Stop any existing updates first
            stopPeriodicUpdates()

            updateRunnable = object : Runnable {
                override fun run() {
                    try {
                        // Only update if fragment is still attached
                        if (isAdded && !isDetached && view != null) {
                            updateLicenseInformation()
                            updateHandler.postDelayed(this, 60000) // Update every minute
                        } else {
                            Log.d("EnhancedSettingsFragment", "Fragment not attached, stopping periodic updates")
                        }
                    } catch (e: Exception) {
                        Log.e("EnhancedSettingsFragment", "Error in periodic update", e)
                    }
                }
            }
            updateHandler.post(updateRunnable!!)
            Log.d("EnhancedSettingsFragment", "Periodic updates started")
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error starting periodic updates", e)
        }
    }

    private fun stopPeriodicUpdates() {
        try {
            updateRunnable?.let {
                updateHandler.removeCallbacks(it)
                Log.d("EnhancedSettingsFragment", "Periodic updates stopped")
            }
            updateRunnable = null
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error stopping periodic updates", e)
        }
    }

    private fun showLanguageDialog() {
        try {
            // Check if fragment is still attached
            if (!isAdded || isDetached) {
                Log.w("EnhancedSettingsFragment", "Fragment not attached, cannot show language dialog")
                return
            }

            val languages = if (languageManager.isChineseEnabled()) {
                arrayOf("中文", "English")
            } else {
                arrayOf("Chinese", "English")
            }

            val currentSelection = if (languageManager.isChineseEnabled()) 0 else 1

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(if (languageManager.isChineseEnabled()) "选择语言" else "Select Language")
                .setSingleChoiceItems(languages, currentSelection) { dialog, which ->
                    try {
                        val enableChinese = (which == 0)
                        languageManager.setChineseEnabled(enableChinese)

                        // Update current fragment if still attached
                        if (isAdded && !isDetached) {
                            updateLanguage()

                            // Notify MainActivity to update all fragments
                            if (activity is MainActivity) {
                                (activity as MainActivity).updateLanguage()
                            }
                        }

                        dialog.dismiss()
                    } catch (e: Exception) {
                        Log.e("EnhancedSettingsFragment", "Error in language selection", e)
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(if (languageManager.isChineseEnabled()) "取消" else "Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error showing language dialog", e)
        }
    }



    fun updateLanguage() {
        try {
            // Check if fragment is still attached and views are available
            if (!isAdded || isDetached || view == null) {
                Log.w("EnhancedSettingsFragment", "Fragment not attached, skipping language update")
                return
            }

            // Update section headers with null checks
            if (::tvLicenseInfoHeader.isInitialized) {
                tvLicenseInfoHeader.text = if (languageManager.isChineseEnabled()) {
                    "许可证信息 :"
                } else {
                    "License Information :"
                }
            }

            if (::tvLanguageSettingsHeader.isInitialized) {
                tvLanguageSettingsHeader.text = if (languageManager.isChineseEnabled()) {
                    "语言设置"
                } else {
                    "Language Settings"
                }
            }

            if (::tvLanguageDescription.isInitialized) {
                tvLanguageDescription.text = if (languageManager.isChineseEnabled()) {
                    "选择您的首选语言"
                } else {
                    "Choose your preferred language"
                }
            }

            // Update current language display with null check
            if (::tvCurrentLanguage.isInitialized) {
                tvCurrentLanguage.text = if (languageManager.isChineseEnabled()) {
                    "切换语言"
                } else {
                    "Switch Language"
                }
            }

            // Refresh all user information with new language
            loadUserInformation()
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error updating language", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Log.d("EnhancedSettingsFragment", "onResume called")
            updateLanguage()
            loadUserInformation()
            startPeriodicUpdates()
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error in onResume", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            Log.d("EnhancedSettingsFragment", "onPause called")
            stopPeriodicUpdates()
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error in onPause", e)
        }
    }

    override fun onDestroyView() {
        try {
            Log.d("EnhancedSettingsFragment", "onDestroyView called")
            stopPeriodicUpdates()
        } catch (e: Exception) {
            Log.e("EnhancedSettingsFragment", "Error in onDestroyView", e)
        }
        super.onDestroyView()
    }

    companion object {
        fun newInstance(): EnhancedSettingsFragment {
            return EnhancedSettingsFragment()
        }
    }
}
