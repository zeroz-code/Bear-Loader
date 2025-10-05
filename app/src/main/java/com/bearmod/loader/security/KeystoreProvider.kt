package com.bearmod.loader.security

/**
 * Abstraction over Android KeyStore operations so production code can use real KeyStore
 * and unit tests can inject a mock implementation.
 */
interface KeystoreProvider {
    /** Ensure a key with [alias] exists. Return true if ready. */
    fun ensureKey(alias: String): Boolean

    /** Encrypt plaintext bytes using the key identified by [alias]. Returns combined IV + ciphertext bytes. */
    fun encrypt(alias: String, plaintext: ByteArray): ByteArray?

    /** Decrypt combined IV + ciphertext using the key identified by [alias]. Returns plaintext bytes or null on error. */
    fun decrypt(alias: String, combined: ByteArray): ByteArray?
}
