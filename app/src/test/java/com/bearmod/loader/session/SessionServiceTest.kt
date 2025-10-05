package com.bearmod.loader.session

import com.bearmod.loader.session.SessionStore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.bearmod.loader.logging.Logger

class SessionServiceTest {

    private lateinit var securePreferences: SessionStore
    private lateinit var logger: Logger
    private lateinit var sessionService: SessionService

    @Before
    fun setUp() {
    securePreferences = mock()
    logger = mock()
    sessionService = SessionService(securePreferences, logger)
    }

    @Test
    fun `clearCorruptedSession calls clearSessionToken and may clear device registration`() {
        // Simulate device not registered
        whenever(securePreferences.isDeviceRegistered()).thenReturn(false)

        sessionService.clearCorruptedSession()

        verify(securePreferences).clearSessionToken()
        verify(securePreferences).clearDeviceRegistration()
    }

    @Test
    fun `getSessionToken delegates to securePreferences`() {
        whenever(securePreferences.getSessionToken()).thenReturn("token123")
        val token = sessionService.getSessionToken()
        assertEquals("token123", token)
    }

}
