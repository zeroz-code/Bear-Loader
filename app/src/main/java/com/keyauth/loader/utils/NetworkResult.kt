package com.keyauth.loader.utils

/**
 * A generic wrapper class for network operations
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()
}

/**
 * Extension function to check if result is successful
 */
fun <T> NetworkResult<T>.isSuccess(): Boolean = this is NetworkResult.Success

/**
 * Extension function to check if result is error
 */
fun <T> NetworkResult<T>.isError(): Boolean = this is NetworkResult.Error

/**
 * Extension function to check if result is loading
 */
fun <T> NetworkResult<T>.isLoading(): Boolean = this is NetworkResult.Loading

/**
 * Extension function to get data if successful, null otherwise
 */
fun <T> NetworkResult<T>.getDataOrNull(): T? = when (this) {
    is NetworkResult.Success -> data
    else -> null
}

/**
 * Extension function to get error message if error, null otherwise
 */
fun <T> NetworkResult<T>.getErrorMessage(): String? = when (this) {
    is NetworkResult.Error -> message
    else -> null
}
