package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface EarningsApiService {
    
    /**
     * Get earnings dashboard
     * GET /earnings/dashboard
     */
    @GET("earnings/dashboard")
    fun getEarningsDashboard(): Call<EarningsDashboardResponse>
    
    /**
     * Request withdrawal
     * POST /withdrawals/request
     */
    @POST("withdrawals/request")
    fun requestWithdrawal(
        @Body request: RequestWithdrawalRequest
    ): Call<RequestWithdrawalResponse>
    
    /**
     * Get withdrawal history
     * GET /withdrawals/history
     */
    @GET("withdrawals/history")
    fun getWithdrawalHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<WithdrawalHistoryResponse>
    
    /**
     * Get bank accounts
     * GET /bank-accounts
     */
    @GET("bank-accounts")
    fun getBankAccounts(): Call<BankAccountsResponse>
    
    /**
     * Add bank account
     * POST /bank-accounts
     */
    @POST("bank-accounts")
    fun addBankAccount(
        @Body request: AddBankAccountRequest
    ): Call<ApiResponse<BankAccountDto>>
    
    /**
     * Update bank account
     * PUT /bank-accounts/{accountId}
     */
    @PUT("bank-accounts/{accountId}")
    fun updateBankAccount(
        @Path("accountId") accountId: String,
        @Body request: AddBankAccountRequest
    ): Call<ApiResponse<BankAccountDto>>
    
    /**
     * Delete bank account
     * DELETE /bank-accounts/{accountId}
     */
    @DELETE("bank-accounts/{accountId}")
    fun deleteBankAccount(
        @Path("accountId") accountId: String
    ): Call<ApiResponse<String>>
}

