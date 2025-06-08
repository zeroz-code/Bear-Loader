package com.keyauth.loader

import android.app.Application
import android.util.Log

/**
 * Application class for KeyAuth Loader
 * Provides global application state management
 *
 * IMPORTANT: Following KeyAuth C++ library v1.3 pattern
 * KeyAuth initialization MUST be called first before any other operations
 */
class KeyAuthLoaderApplication : Application() {

    companion object {
        private const val TAG = "KeyAuthLoaderApp"

        @Volatile
        private var instance: KeyAuthLoaderApplication? = null

        fun getInstance(): KeyAuthLoaderApplication? = instance

        // Global KeyAuth initialization state following C++ pattern
        @Volatile
        private var globalKeyAuthInitialized = false

        fun isKeyAuthGloballyInitialized(): Boolean = globalKeyAuthInitialized

        fun setKeyAuthGloballyInitialized(initialized: Boolean) {
            globalKeyAuthInitialized = initialized
            Log.d(TAG, "KeyAuth global initialization state: $initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        Log.d(TAG, "🚀 KeyAuth Loader Application starting...")
        Log.d(TAG, "📱 Following KeyAuth C++ library v1.3 initialization pattern")

        // Reset global KeyAuth state on app start
        globalKeyAuthInitialized = false

        // Initialize any app-wide components here
        // For example: crash reporting, analytics, etc.

        // CRITICAL: KeyAuth initialization is handled per-activity following C++ pattern
        // This ensures proper KeyAuthApp.init() -> KeyAuthApp.response.success sequence

        Log.d(TAG, "✅ Application initialization complete")
    }

    override fun onTerminate() {
        super.onTerminate()
        instance = null
        globalKeyAuthInitialized = false
        Log.d(TAG, "🔄 KeyAuth Loader Application terminated")
    }
}
