package com.onlycare.app.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Interceptor to handle common network errors
 */
class ErrorInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "ErrorInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        try {
            val response = chain.proceed(request)
            
            // Handle HTTP error responses
            when (response.code) {
                401 -> {
                    // Unauthorized - token expired or invalid
                    Log.w(TAG, "401 Unauthorized - Token may be expired")
                    // TODO: Trigger token refresh or logout
                }
                403 -> {
                    // Forbidden
                    Log.w(TAG, "403 Forbidden - Access denied")
                }
                404 -> {
                    // Not Found
                    Log.w(TAG, "404 Not Found - Resource doesn't exist")
                }
                422 -> {
                    // Validation Error
                    Log.w(TAG, "422 Validation Error")
                }
                500 -> {
                    // Server Error
                    Log.e(TAG, "500 Internal Server Error")
                }
                502, 503, 504 -> {
                    // Server Unavailable
                    Log.e(TAG, "${response.code} Server Unavailable")
                }
            }
            
            return response
            
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Request timeout: ${e.message}")
            throw NetworkException("Request timeout. Please check your connection.", e)
            
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No internet connection: ${e.message}")
            throw NetworkException("No internet connection. Please check your network.", e)
            
        } catch (e: Exception) {
            Log.e(TAG, "Network error: ${e.message}")
            throw NetworkException("Network error occurred. Please try again.", e)
        }
    }
}

/**
 * Custom exception for network errors
 */
class NetworkException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)

