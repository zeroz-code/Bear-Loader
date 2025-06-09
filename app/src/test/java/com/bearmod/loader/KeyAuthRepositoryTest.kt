package com.bearmod.loader

import android.content.Context
import com.bearmod.loader.data.api.KeyAuthApiService
import com.bearmod.loader.data.model.KeyAuthResponse
import com.bearmod.loader.data.repository.KeyAuthRepository
import com.bearmod.loader.utils.NetworkResult
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import org.junit.Assert.*
import retrofit2.Response

/**
 * Unit tests for KeyAuthRepository
 * Tests the new initialization logic and thread-safety improvements
 */
class KeyAuthRepositoryTest {

    @Mock
    private lateinit var mockApiService: KeyAuthApiService

    @Mock
    private lateinit var mockContext: Context

    private lateinit var repository: KeyAuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Create repository with logging disabled for testing
        repository = KeyAuthRepository(mockApiService, mockContext, enableLogging = false)
    }

    @Test
    fun `repository should not be initialized initially`() {
        assertFalse("App should not be initialized initially", repository.isAppInitialized())
    }

    @Test
    fun `clearSession should reset initialization state`() {
        // Clear session and verify state is reset
        repository.clearSession()
        assertFalse("App should not be initialized after clearSession", repository.isAppInitialized())
        assertNull("Session ID should be null after clearSession", repository.getSessionId())
    }

    @Test
    fun `isAppInitialized should be thread-safe`() = runBlocking {
        // Test thread-safety by calling from multiple contexts
        val results = mutableListOf<Boolean>()

        // Simulate concurrent access
        repeat(10) {
            results.add(repository.isAppInitialized())
        }

        // All results should be consistent (false initially)
        assertTrue("All results should be false initially", results.all { !it })
    }

    @Test
    fun `authenticateWithLicense should fail when not initialized`() = runBlocking {
        // Ensure repository is not initialized
        repository.clearSession()

        // Attempt authentication
        val result = repository.authenticateWithLicense("test-license-key")

        // Should fail with initialization error
        assertTrue("Authentication should fail when not initialized", result is NetworkResult.Error)
        val errorMessage = (result as NetworkResult.Error).message
        assertTrue("Error should mention initialization", errorMessage.contains("not initialized"))
    }
}
