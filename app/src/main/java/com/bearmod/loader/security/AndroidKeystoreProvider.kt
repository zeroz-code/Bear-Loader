package com.bearmod.loader.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidKeystoreProvider : KeystoreProvider {
    companion object {
        private const val TAG = "AndroidKeystoreProvider"
        private const val PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 16
    }

    override fun ensureKey(alias: String): Boolean {
        try {
            val keyStore = KeyStore.getInstance(PROVIDER)
            keyStore.load(null)
            if (!keyStore.containsAlias(alias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
                val spec = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(false)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
                Log.d(TAG, "Generated new keystore key: $alias")
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to ensure key: $alias", e)
            return false
        }
    }

    override fun encrypt(alias: String, plaintext: ByteArray): ByteArray? {
        try {
            val keyStore = KeyStore.getInstance(PROVIDER)
            keyStore.load(null)
            val secretKey = keyStore.getKey(alias, null) as? SecretKey ?: return null
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plaintext)
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            return combined
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            return null
        }
    }

    override fun decrypt(alias: String, combined: ByteArray): ByteArray? {
        try {
            val keyStore = KeyStore.getInstance(PROVIDER)
            keyStore.load(null)
            val secretKey = keyStore.getKey(alias, null) as? SecretKey ?: return null
            val ivLength = 12 // GCM IV length
            val iv = ByteArray(ivLength)
            val encrypted = ByteArray(combined.size - ivLength)
            System.arraycopy(combined, 0, iv, 0, ivLength)
            System.arraycopy(combined, ivLength, encrypted, 0, encrypted.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            return cipher.doFinal(encrypted)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            return null
        }
    }
}
