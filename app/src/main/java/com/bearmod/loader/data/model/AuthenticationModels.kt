package com.bearmod.loader.data.model

import com.google.gson.annotations.SerializedName

/**
 * Enhanced authentication models for session persistence and HWID-based authentication
 */

/**
 * Authentication state for session management
 */
data class AuthenticationState(
    val isAuthenticated: Boolean = false,
    val sessionToken: String? = null,
    val refreshToken: String? = null,
    val expiryTime: Long = 0L,
    val hwid: String? = null,
    val licenseKey: String? = null,
    val userInfo: UserInfo? = null,
    val deviceTrustLevel: Int = 0,
    val isDeviceRegistered: Boolean = false
) {
    /**
     * Check if session is expired
     */
    fun isSessionExpired(): Boolean {
        return expiryTime > 0L && System.currentTimeMillis() > expiryTime
    }

    /**
     * Check if session is valid (authenticated and not expired)
     */
    fun isSessionValid(): Boolean {
        return isAuthenticated && !sessionToken.isNullOrEmpty() && !isSessionExpired()
    }

    /**
     * Get time until expiry in milliseconds
     */
    fun getTimeUntilExpiry(): Long {
        return if (expiryTime > 0L) {
            maxOf(0L, expiryTime - System.currentTimeMillis())
        } else {
            0L
        }
    }
}

/**
 * Device registration information
 */
data class DeviceRegistration(
    val hwid: String,
    val licenseKey: String,
    val registrationTime: Long = System.currentTimeMillis(),
    val trustLevel: Int = 0,
    val lastAuthTime: Long = 0L,
    val authCount: Int = 0
)

/**
 * Session restoration result
 */
sealed class SessionRestoreResult {
    object NoStoredSession : SessionRestoreResult()
    object SessionExpired : SessionRestoreResult()
    object HWIDMismatch : SessionRestoreResult()
    data class Success(val authState: AuthenticationState) : SessionRestoreResult()
    data class Failed(val error: String) : SessionRestoreResult()
}

/**
 * HWID validation result
 */
sealed class HWIDValidationResult {
    object Valid : HWIDValidationResult()
    object Changed : HWIDValidationResult()
    object Invalid : HWIDValidationResult()
    data class Error(val message: String) : HWIDValidationResult()
}

/**
 * Enhanced KeyAuth response with session management
 */
data class EnhancedKeyAuthResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("sessionid")
    val sessionId: String? = null,
    
    @SerializedName("info")
    val userInfo: UserInfo? = null,
    
    // Enhanced fields for session management
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    
    @SerializedName("expires_in")
    val expiresIn: Long? = null,
    
    @SerializedName("device_registered")
    val deviceRegistered: Boolean? = null,
    
    @SerializedName("trust_level")
    val trustLevel: Int? = null
) {
    /**
     * Convert to AuthenticationState
     */
    fun toAuthenticationState(hwid: String, licenseKey: String): AuthenticationState {
        val expiryTime = if (expiresIn != null && expiresIn > 0) {
            System.currentTimeMillis() + (expiresIn * 1000)
        } else {
            0L
        }

        return AuthenticationState(
            isAuthenticated = success,
            sessionToken = sessionId,
            refreshToken = refreshToken,
            expiryTime = expiryTime,
            hwid = hwid,
            licenseKey = licenseKey,
            userInfo = userInfo,
            deviceTrustLevel = trustLevel ?: 0,
            isDeviceRegistered = deviceRegistered ?: false
        )
    }
}

/**
 * Auto-login configuration
 */
data class AutoLoginConfig(
    val enabled: Boolean = false,
    val rememberDevice: Boolean = false,
    val trustThreshold: Int = 1, // Minimum trust level for auto-login
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000L
)

/**
 * Session persistence configuration
 */
data class SessionPersistenceConfig(
    val enableSessionPersistence: Boolean = true,
    val enableRefreshTokens: Boolean = true,
    val sessionTimeoutMs: Long = 24 * 60 * 60 * 1000L, // 24 hours
    val refreshTokenTimeoutMs: Long = 7 * 24 * 60 * 60 * 1000L, // 7 days
    val maxTrustLevel: Int = 3,
    val hwidValidationEnabled: Boolean = true
)

/**
 * Authentication flow state
 */
enum class AuthFlowState {
    IDLE,
    INITIALIZING,
    CHECKING_STORED_SESSION,
    VALIDATING_HWID,
    AUTHENTICATING_WITH_LICENSE,
    REFRESHING_TOKEN,
    REGISTERING_DEVICE,
    AUTHENTICATED,
    FAILED,
    SESSION_EXPIRED,
    HWID_MISMATCH
}

/**
 * Authentication error types
 */
enum class AuthErrorType {
    NETWORK_ERROR,
    INVALID_LICENSE,
    SESSION_EXPIRED,
    HWID_MISMATCH,
    DEVICE_NOT_REGISTERED,
    INITIALIZATION_FAILED,
    TOKEN_REFRESH_FAILED,
    UNKNOWN_ERROR
}

/**
 * Authentication error with detailed information
 */
data class AuthError(
    val type: AuthErrorType,
    val message: String,
    val canRetry: Boolean = false,
    val requiresUserAction: Boolean = false,
    val suggestedAction: String? = null
)
