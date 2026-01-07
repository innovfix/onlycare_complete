package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Coin Package DTO
 */
data class CoinPackageDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("coins")
    val coins: Int,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("original_price")
    val originalPrice: Double,
    
    @SerializedName("discount")
    val discount: Int,
    
    @SerializedName("is_popular")
    val isPopular: Boolean = false,
    
    @SerializedName("is_best_value")
    val isBestValue: Boolean = false
)

/**
 * Get Packages Response
 */
data class GetPackagesResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("packages")
    val packages: List<CoinPackageDto>
)

/**
 * Initiate Purchase Request
 */
data class InitiatePurchaseRequest(
    @SerializedName("package_id")
    val packageId: String,
    
    @SerializedName("payment_method")
    val paymentMethod: String // "PhonePe", "GooglePay", "Paytm", "UPI", "Card"
)

/**
 * Initiate Purchase Response
 */
data class InitiatePurchaseResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("transaction")
    val transaction: TransactionDto,
    
    @SerializedName("payment_gateway_url")
    val paymentGatewayUrl: String,
    
    @SerializedName("payment_gateway_data")
    val paymentGatewayData: Map<String, String>
)

/**
 * Transaction DTO
 */
data class TransactionDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("coins")
    val coins: Int,
    
    @SerializedName("is_credit")
    val isCredit: Boolean = true,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("payment_method")
    val paymentMethod: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("date")
    val date: String? = null,
    
    @SerializedName("time")
    val time: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("icon_type")
    val iconType: String? = null
)

/**
 * Verify Purchase Request
 */
data class VerifyPurchaseRequest(
    @SerializedName("transaction_id")
    val transactionId: String,
    
    @SerializedName("payment_gateway_id")
    val paymentGatewayId: String,
    
    @SerializedName("status")
    val status: String // "SUCCESS" or "FAILED"
)

/**
 * Get Balance Response
 */
data class GetBalanceResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("coin_balance")
    val coinBalance: Int
)

/**
 * Generic Wallet Response
 */
data class WalletResponse(
    @SerializedName("coin_balance")
    val coinBalance: Int
)

/**
 * Best Offer DTO
 */
data class BestOfferDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("discount_price")
    val discountPrice: Double,
    
    @SerializedName("coins")
    val coins: Int,
    
    @SerializedName("save")
    val save: Int,
    
    @SerializedName("popular")
    val popular: Int,
    
    @SerializedName("total_count")
    val totalCount: Int,
    
    @SerializedName("best_offer")
    val bestOffer: Int,
    
    @SerializedName("pg")
    val pg: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Best Offers Response
 */
data class BestOffersResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("data")
    val data: List<BestOfferDto>
)