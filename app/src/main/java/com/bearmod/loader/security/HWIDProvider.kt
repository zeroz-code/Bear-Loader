package com.bearmod.loader.security

/**
 * Abstraction for providing a stable, testable Hardware ID (HWID) for the device.
 * Implementations should return a persistent identifier and may persist it using
 * secure storage. The interface keeps the API intentionally small for easy mocking.
 */
interface HWIDProvider {
    /**
     * Return an existing HWID or generate/persist a new one if none exists.
     */
    fun getHWID(): String

    /**
     * Clear stored HWID (used in tests or when resetting device registration)
     */
    fun clearHWID()
}
