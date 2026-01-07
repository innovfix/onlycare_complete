package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("error")
    val error: ApiError? = null
)

/**
 * Error response
 */
data class ApiError(
    @SerializedName("code")
    val code: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("details")
    val details: Map<String, List<String>>? = null
)

/**
 * Pagination info
 */
data class PaginatedResponse<T>(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: List<T> = emptyList(),
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)

data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("total_items")
    val totalItems: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    @SerializedName("has_next")
    val hasNext: Boolean,
    
    @SerializedName("has_prev")
    val hasPrev: Boolean
)

