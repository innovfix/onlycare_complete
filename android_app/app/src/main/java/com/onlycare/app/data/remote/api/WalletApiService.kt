package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface WalletApiService {
    
    @GET("wallet/packages")
    fun getPackages(): Call<GetPackagesResponse>
    
    @POST("wallet/purchase")
    fun initiatePurchase(
        @Body request: InitiatePurchaseRequest
    ): Call<InitiatePurchaseResponse>
    
    @POST("wallet/verify-purchase")
    fun verifyPurchase(
        @Body request: VerifyPurchaseRequest
    ): Call<ApiResponse<TransactionDto>>
    
    @GET("wallet/transactions")
    fun getTransactionHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<PaginatedResponse<TransactionDto>>
    
    @GET("wallet/balance")
    fun getBalance(): Call<GetBalanceResponse>
    
    @POST("wallet/best-offers")
    fun getBestOffers(): Call<BestOffersResponse>
}

