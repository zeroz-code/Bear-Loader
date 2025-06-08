package com.keyauth.loader.data.api

import com.keyauth.loader.data.model.AppInfo
import com.keyauth.loader.data.model.KeyAuthResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * KeyAuth API service interface for v1.3 API
 * Note: API v1.3 does NOT require application secret
 */
interface KeyAuthApiService {
    
    /**
     * Initialize the application
     * Added hash parameter for KeyAuth integrity checking
     */
    @FormUrlEncoded
    @POST(".")
    suspend fun init(
        @Field("type") type: String = "init",
        @Field("ver") version: String,
        @Field("name") name: String,
        @Field("ownerid") ownerId: String,
        @Field("hash") hash: String? = null
    ): Response<KeyAuthResponse>
    
    /**
     * Authenticate with license key
     */
    @FormUrlEncoded
    @POST(".")
    suspend fun license(
        @Field("type") type: String = "license",
        @Field("key") licenseKey: String,
        @Field("hwid") hwid: String,
        @Field("sessionid") sessionId: String,
        @Field("name") name: String,
        @Field("ownerid") ownerId: String
    ): Response<KeyAuthResponse>
    
    /**
     * Check session validity
     */
    @FormUrlEncoded
    @POST(".")
    suspend fun checkSession(
        @Field("type") type: String = "check",
        @Field("sessionid") sessionId: String,
        @Field("name") name: String,
        @Field("ownerid") ownerId: String
    ): Response<KeyAuthResponse>
    
    /**
     * Get application statistics
     */
    @FormUrlEncoded
    @POST(".")
    suspend fun fetchStats(
        @Field("type") type: String = "fetchstats",
        @Field("sessionid") sessionId: String,
        @Field("name") name: String,
        @Field("ownerid") ownerId: String
    ): Response<AppInfo>
    
    /**
     * Log user activity
     */
    @FormUrlEncoded
    @POST(".")
    suspend fun log(
        @Field("type") type: String = "log",
        @Field("pcuser") pcUser: String,
        @Field("message") message: String,
        @Field("sessionid") sessionId: String,
        @Field("name") name: String,
        @Field("ownerid") ownerId: String
    ): Response<KeyAuthResponse>
}
