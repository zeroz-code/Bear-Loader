package com.bearmod.loader.data.model

import com.google.gson.annotations.SerializedName

/**
 * Base response model for KeyAuth API
 */
data class KeyAuthResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("sessionid")
    val sessionId: String? = null,
    
    @SerializedName("info")
    val userInfo: UserInfo? = null
)

/**
 * User information returned after successful authentication
 */
data class UserInfo(
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("ip")
    val ip: String? = null,
    
    @SerializedName("hwid")
    val hwid: String? = null,
    
    @SerializedName("createdate")
    val createDate: String? = null,
    
    @SerializedName("lastlogin")
    val lastLogin: String? = null,
    
    @SerializedName("subscriptions")
    val subscriptions: List<Subscription>? = null
)

/**
 * Subscription information
 */
data class Subscription(
    @SerializedName("subscription")
    val name: String,
    
    @SerializedName("expiry")
    val expiry: String,
    
    @SerializedName("timeleft")
    val timeLeft: Long? = null
)

/**
 * Application information response
 */
data class AppInfo(
    @SerializedName("numUsers")
    val numUsers: String? = null,
    
    @SerializedName("numOnlineUsers") 
    val numOnlineUsers: String? = null,
    
    @SerializedName("numKeys")
    val numKeys: String? = null,
    
    @SerializedName("version")
    val version: String? = null,
    
    @SerializedName("customerPanelLink")
    val customerPanelLink: String? = null
)
