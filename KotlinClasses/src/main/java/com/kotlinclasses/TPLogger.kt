package com.kotlinclasses

import android.util.Log

object TPLogger {
    private const val TAG = "TPFirebaseChat"
    private var isDebugEnabled = true

    fun d(message: String) {
        if (isDebugEnabled) {
            Log.d(TAG, message)
            println("🔍 DEBUG: $message")
        }
    }

    fun i(message: String) {
        Log.i(TAG, message)
        println("ℹ️ INFO: $message")
    }

    fun e(message: String, error: Throwable? = null) {
        Log.e(TAG, message, error)
        println("❌ ERROR: $message")
        error?.let { println("Stack trace: ${it.stackTraceToString()}") }
    }

    fun w(message: String) {
        Log.w(TAG, message)
        println("⚠️ WARNING: $message")
    }

    fun setDebugEnabled(enabled: Boolean) {
        isDebugEnabled = enabled
    }
}
