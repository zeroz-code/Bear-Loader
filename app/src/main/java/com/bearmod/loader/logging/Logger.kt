package com.bearmod.loader.logging

/** Lightweight logger abstraction so production code can use android.util.Log while tests can inject a no-op/mock. */
interface Logger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
