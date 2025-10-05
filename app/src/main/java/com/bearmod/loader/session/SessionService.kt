package com.bearmod.loader.session

import com.bearmod.loader.logging.AndroidLogger
import com.bearmod.loader.logging.Logger

/**
 * SessionService: thin wrapper around SecurePreferences for session-related operations.
 * Keeps session clearing logic in a single place so callers don't duplicate sequence.
 */
class SessionService(private val sessionStore: SessionStore, private val logger: Logger = AndroidLogger()) {

    companion object {
        private const val TAG = "SessionService"
    }

    /**
     * Returns the stored session token or null if none.
     */
    fun getSessionToken(): String? = sessionStore.getSessionToken()

    /**
     * Store session token with optional expiry timestamp.
     */
    fun storeSessionToken(token: String, expiryTimeMillis: Long = 0L) {
        sessionStore.storeSessionToken(token, expiryTimeMillis)
    }

    /**
     * Clear the persistent session token and related data that may cause server-side
     * "session not found" issues. This preserves non-auth preferences such as remember/autologin flags.
     */
    fun clearCorruptedSession() {
        try {
            logger.d(TAG, "Clearing corrupted session data via SessionService")
            sessionStore.clearSessionToken()

            // If device registration is inconsistent or causing issues, clear it too
            if (!sessionStore.isDeviceRegistered()) {
                sessionStore.clearDeviceRegistration()
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error while clearing corrupted session data", e)
        }
    }

}
