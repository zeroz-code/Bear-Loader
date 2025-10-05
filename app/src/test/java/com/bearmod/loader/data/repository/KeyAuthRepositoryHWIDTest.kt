package com.bearmod.loader.data.repository

import androidx.test.core.app.ApplicationProvider
import com.bearmod.loader.data.api.KeyAuthApiService
import com.bearmod.loader.data.model.KeyAuthResponse
import com.bearmod.loader.security.HWIDProvider
import com.bearmod.loader.utils.SecurePreferences
import com.bearmod.loader.utils.NetworkResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import retrofit2.Response

class KeyAuthRepositoryHWIDTest {

    private lateinit var apiService: KeyAuthApiService
    private lateinit var hwidProvider: HWIDProvider
    private lateinit var securePreferences: SecurePreferences
    private lateinit var repository: KeyAuthRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        securePreferences = SecurePreferences(context)

        apiService = mock()
        hwidProvider = mock()

        repository = KeyAuthRepository(apiService, context, enableLogging = false, hwidProvider = hwidProvider)
    }

    @Test
    fun `restoreSession returns HWIDMismatch when HWID differs`() = runBlocking {
        // Prepare stored values
        securePreferences.clearAll()
        securePreferences.storeSessionToken("stored-session", System.currentTimeMillis() + 3600000)
        securePreferences.setDeviceRegistered("old-hwid", "license")
        securePreferences.storeHWID("old-hwid")

        // HWID provider returns a different HWID
        doReturn("new-hwid").whenever(hwidProvider).getHWID()

        val result = repository.restoreSession()

        assertEquals(true, result is com.bearmod.loader.data.model.SessionRestoreResult.HWIDMismatch)
    }

    @Test
    fun `restoreSession succeeds when HWID matches and session valid`() = runBlocking {
        securePreferences.clearAll()
        securePreferences.storeSessionToken("stored-session", System.currentTimeMillis() + 3600000)
        securePreferences.setDeviceRegistered("the-hwid", "license")
        securePreferences.storeHWID("the-hwid")

        doReturn("the-hwid").whenever(hwidProvider).getHWID()

        // Mock init and checkSession API calls used during restore
        val successResp = KeyAuthResponse(success = true, message = "ok", sessionId = "stored-session")
        doReturn(Response.success(successResp)).whenever(apiService).init(any(), any(), any(), any(), any())
        doReturn(Response.success(successResp)).whenever(apiService).checkSession(any(), any(), any())

        val result = repository.restoreSession()
        assertTrue(result is com.bearmod.loader.data.model.SessionRestoreResult.Success)
    }

    @Test
    fun `attemptHWIDBasedAuth authenticates with bound license`() = runBlocking {
        securePreferences.clearAll()
        // store a bound license
        securePreferences.setDeviceRegistered("the-hwid", "licenseKey123")
        securePreferences.storeHWID("the-hwid")

        doReturn("the-hwid").whenever(hwidProvider).getHWID()

        val authResp = KeyAuthResponse(success = true, message = "ok", sessionId = "new-session")
        doReturn(Response.success(authResp)).whenever(apiService).init(any(), any(), any(), any(), any())
        doReturn(Response.success(authResp)).whenever(apiService).license(any(), any(), any(), any(), any())

        val result = repository.attemptHWIDBasedAuth()
        assertTrue(result is NetworkResult.Success)
        assertEquals("new-session", (result as NetworkResult.Success).data.sessionId)
    }
}
