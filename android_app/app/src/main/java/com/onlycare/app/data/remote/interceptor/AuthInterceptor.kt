package com.onlycare.app.data.remote.interceptor

import android.util.Log
import com.onlycare.app.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor to add authentication token to requests
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    
    companion object {
        private const val TAG = "AuthInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        
        // Get token from session
        val token = sessionManager.getAuthToken()
        
        // Log token status (without exposing full token for security)
        if (token.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ No auth token found for request: ${originalRequest.method} $url")
            Log.w(TAG, "   This request may fail with 401 Unauthorized")
        } else {
            // Log token presence and length (for debugging) but not the actual token
            val tokenPreview = if (token.length > 20) {
                "${token.take(10)}...${token.takeLast(10)}"
            } else {
                "*".repeat(token.length)
            }
            Log.d(TAG, "✓ Auth token found (length: ${token.length}, preview: $tokenPreview)")
            Log.d(TAG, "  Request: ${originalRequest.method} $url")
        }
        
        // Build request with common headers
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("ngrok-skip-browser-warning", "true") // Skip ngrok warning page
        
        // Add Authorization header if token exists
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        } else {
            Log.w(TAG, "⚠️ Authorization header NOT added - token is null or empty")
        }
        
        val authenticatedRequest = requestBuilder.build()
        
        // Execute request
        val response = chain.proceed(authenticatedRequest)
        
        // Handle 401 Unauthorized responses
        if (response.code == 401) {
            Log.e(TAG, "═══════════════════════════════════════════════════════════")
            Log.e(TAG, "❌ 401 UNAUTHORIZED - Authentication Failed")
            Log.e(TAG, "═══════════════════════════════════════════════════════════")
            Log.e(TAG, "Request URL: $url")
            Log.e(TAG, "Request Method: ${originalRequest.method}")
            Log.e(TAG, "Token Status: ${if (token.isNullOrEmpty()) "MISSING" else "PRESENT (but invalid/expired)"}")
            if (!token.isNullOrEmpty()) {
                Log.e(TAG, "Token Length: ${token.length}")
            }
            Log.e(TAG, "═══════════════════════════════════════════════════════════")
            Log.e(TAG, "Possible causes:")
            Log.e(TAG, "  1. User is not logged in (token is null/empty)")
            Log.e(TAG, "  2. Token has expired")
            Log.e(TAG, "  3. Token is invalid")
            Log.e(TAG, "  4. Backend authentication middleware is rejecting the token")
            Log.e(TAG, "═══════════════════════════════════════════════════════════")
        }
        
        return response
    }
}

