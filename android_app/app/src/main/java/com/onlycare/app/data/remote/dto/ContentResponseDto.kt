package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Privacy Policy Response DTO
 */
data class PrivacyPolicyResponseDto(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: PrivacyPolicyDataDto
)

data class PrivacyPolicyDataDto(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("last_updated")
    val lastUpdated: String? = null,

    /**
     * Structured content sections (preferred by the app UI).
     * Some servers may send HTML only; keep default to avoid parsing crashes.
     */
    @SerializedName("content")
    val content: List<ContentSectionDto>? = null,

    /**
     * Optional HTML policy content (used when server stores policy as HTML).
     */
    @SerializedName("html_content")
    val htmlContent: String? = null
)

data class ContentSectionDto(
    @SerializedName("heading")
    val heading: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("points")
    val points: List<String>? = null
)

/**
 * Terms & Conditions Response DTO
 */
data class TermsResponseDto(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: TermsDataDto
)

data class TermsDataDto(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("last_updated")
    val lastUpdated: String? = null,

    @SerializedName("content")
    val content: List<ContentSectionDto>? = null,

    @SerializedName("html_content")
    val htmlContent: String? = null
)

/**
 * Refund Policy Response DTO
 */
data class RefundPolicyResponseDto(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: RefundPolicyDataDto
)

data class RefundPolicyDataDto(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("last_updated")
    val lastUpdated: String? = null,

    @SerializedName("content")
    val content: List<ContentSectionDto>? = null,

    @SerializedName("html_content")
    val htmlContent: String? = null
)

/**
 * Community Guidelines Response DTO
 */
data class CommunityGuidelinesResponseDto(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: CommunityGuidelinesDataDto
)

data class CommunityGuidelinesDataDto(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("last_updated")
    val lastUpdated: String? = null,

    @SerializedName("content")
    val content: List<ContentSectionDto>? = null,

    @SerializedName("html_content")
    val htmlContent: String? = null
)

