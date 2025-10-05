package com.bearmod.loader.utils

import android.content.Context

/**
 * Kotlin-friendly session service that delegates to the Java SessionManager.
 * Use this from Kotlin code to keep session-related logic in one place.
 */
class SessionService(context: Context) {
    private val sessionManager = SessionManager(context)

    fun clearSessionToken() {
        sessionManager.clearSessionToken()
    }

    fun clearAuthenticationData() {
        sessionManager.clearAuthenticationData()
    }

    fun clearAll() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}
