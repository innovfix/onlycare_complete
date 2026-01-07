package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * KYC Status Response
 */
data class KycStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("kyc_status")
    val kycStatus: String, // "PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"
    
    @SerializedName("documents")
    val documents: List<KycDocumentDto>? = null,
    
    @SerializedName("rejection_reason")
    val rejectionReason: String? = null
)

/**
 * KYC Document DTO
 */
data class KycDocumentDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("document_type")
    val documentType: String, // "AADHAAR", "PAN", "SELFIE"
    
    @SerializedName("document_number")
    val documentNumber: String? = null,
    
    @SerializedName("document_url")
    val documentUrl: String,
    
    @SerializedName("status")
    val status: String
)

/**
 * Submit KYC Request
 */
data class SubmitKycRequest(
    @SerializedName("aadhaar_number")
    val aadhaarNumber: String,
    
    @SerializedName("pan_number")
    val panNumber: String,
    
    @SerializedName("aadhaar_front")
    val aadhaarFront: String, // Base64 or URL
    
    @SerializedName("aadhaar_back")
    val aadhaarBack: String,
    
    @SerializedName("pan_card")
    val panCard: String,
    
    @SerializedName("selfie")
    val selfie: String
)

