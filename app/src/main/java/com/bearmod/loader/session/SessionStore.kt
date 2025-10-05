package com.bearmod.loader.session

/**
 * Abstraction for session-related persistent storage operations.
 * Allows tests to mock storage easily without requiring mocking final classes.
 */
interface SessionStore {
    fun getSessionToken(): String?
    fun storeSessionToken(sessionToken: String, expiryTimeMillis: Long)
    fun clearSessionToken()
    fun isDeviceRegistered(): Boolean
    fun clearDeviceRegistration()
}
