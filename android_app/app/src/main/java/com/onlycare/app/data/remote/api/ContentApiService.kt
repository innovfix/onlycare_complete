package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.http.GET

/**
 * Content API Service
 * 
 * Handles fetching app content like privacy policy, terms, etc.
 * Public endpoints - no authentication required
 */
interface ContentApiService {
    
    /**
     * Get Privacy Policy
     * GET /content/privacy-policy
     */
    @GET("content/privacy-policy")
    fun getPrivacyPolicy(): Call<PrivacyPolicyResponseDto>
    
    /**
     * Get Terms & Conditions
     * GET /content/terms-conditions
     */
    @GET("content/terms-conditions")
    fun getTermsAndConditions(): Call<TermsResponseDto>
    
    /**
     * Get Refund & Cancellation Policy
     * GET /content/refund-policy
     */
    @GET("content/refund-policy")
    fun getRefundPolicy(): Call<RefundPolicyResponseDto>
    
    /**
     * Get Community Guidelines & Moderation Policy
     * GET /content/community-guidelines
     */
    @GET("content/community-guidelines")
    fun getCommunityGuidelines(): Call<CommunityGuidelinesResponseDto>
}

