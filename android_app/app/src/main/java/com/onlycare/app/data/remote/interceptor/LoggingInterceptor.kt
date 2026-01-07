package com.onlycare.app.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException

/**
 * Interceptor to log HTTP requests and responses
 */
class LoggingInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "API"
        private const val MAX_BODY_SIZE = 1000L // Limit body logging to 1000 characters
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Log request
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "→ REQUEST")
        Log.d(TAG, "URL: ${request.url}")
        Log.d(TAG, "Method: ${request.method}")
        Log.d(TAG, "Headers: ${request.headers}")
        
        // Log request body if present
        request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                val bodyString = buffer.readUtf8()
                Log.d(TAG, "Body: ${bodyString.take(MAX_BODY_SIZE.toInt())}")
            } catch (e: IOException) {
                Log.e(TAG, "Error reading request body: ${e.message}")
            }
        }
        
        // Execute request
        val startTime = System.currentTimeMillis()
        val response: Response
        
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "← RESPONSE FAILED")
            Log.e(TAG, "Error: ${e.message}")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            throw e
        }
        
        val duration = System.currentTimeMillis() - startTime
        
        // Log response
        Log.d(TAG, "← RESPONSE")
        Log.d(TAG, "Duration: ${duration}ms")
        Log.d(TAG, "Status Code: ${response.code}")
        Log.d(TAG, "Status Message: ${response.message}")
        Log.d(TAG, "Headers: ${response.headers}")
        
        // Log response body
        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer
        val bodyString = buffer?.clone()?.readUtf8() ?: ""
        
        Log.d(TAG, "Body: ${bodyString.take(MAX_BODY_SIZE.toInt())}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        return response
    }
}

