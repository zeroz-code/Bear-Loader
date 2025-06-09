package com.bearmod.loader.data.api

import com.bearmod.loader.data.model.OTAResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * API service for OTA update system
 */
interface OTAApiService {
    
    /**
     * Get version information from the configured endpoint
     */
    @GET
    suspend fun getVersionInfo(@Url url: String): Response<OTAResponse>
    
    /**
     * Download a file with streaming support for progress tracking
     */
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>
}
