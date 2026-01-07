package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Avatar DTO matching API response
 */
data class AvatarDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("gender")
    val gender: String, // "MALE" or "FEMALE"
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * Get Avatars Response
 */
data class GetAvatarsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("gender")
    val gender: String, // "MALE" or "FEMALE"
    
    @SerializedName("count")
    val count: Int,
    
    @SerializedName("avatars")
    val avatars: List<AvatarDto>
)









