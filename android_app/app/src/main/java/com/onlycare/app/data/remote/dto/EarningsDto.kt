package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Earnings Dashboard Response
 */
data class EarningsDashboardResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("dashboard")
    val dashboard: EarningsDashboardDto
)

/**
 * Earnings Dashboard DTO
 */
data class EarningsDashboardDto(
    @SerializedName("total_earnings")
    val totalEarnings: Double,
    
    @SerializedName("today_earnings")
    val todayEarnings: Double,
    
    @SerializedName("week_earnings")
    val weekEarnings: Double,
    
    @SerializedName("month_earnings")
    val monthEarnings: Double,
    
    @SerializedName("available_balance")
    val availableBalance: Double,
    
    @SerializedName("pending_withdrawals")
    val pendingWithdrawals: Double,
    
    @SerializedName("total_calls")
    val totalCalls: Int,
    
    @SerializedName("today_calls")
    val todayCalls: Int,
    
    @SerializedName("average_call_duration")
    val averageCallDuration: Int,
    
    @SerializedName("average_earnings_per_call")
    val averageEarningsPerCall: Double,
    
    @SerializedName("audio_calls_count")
    val audioCallsCount: Int,
    
    @SerializedName("video_calls_count")
    val videoCallsCount: Int
)

/**
 * Request Withdrawal Request
 */
data class RequestWithdrawalRequest(
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("bank_account_id")
    val bankAccountId: String
)

/**
 * Withdrawal DTO
 */
data class WithdrawalDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("status")
    val status: String, // "PENDING", "APPROVED", "COMPLETED", "REJECTED"
    
    @SerializedName("bank_account")
    val bankAccount: BankAccountDto,
    
    @SerializedName("requested_at")
    val requestedAt: String,
    
    @SerializedName("processed_at")
    val processedAt: String? = null,
    
    @SerializedName("rejection_reason")
    val rejectionReason: String? = null
)

/**
 * Bank Account DTO
 */
data class BankAccountDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("account_holder_name")
    val accountHolderName: String,
    
    @SerializedName("account_number")
    val accountNumber: String,
    
    @SerializedName("ifsc_code")
    val ifscCode: String,
    
    @SerializedName("bank_name")
    val bankName: String? = null,
    
    @SerializedName("upi_id")
    val upiId: String? = null,
    
    @SerializedName("is_primary")
    val isPrimary: Boolean = false
)

/**
 * Add Bank Account Request
 */
data class AddBankAccountRequest(
    @SerializedName("account_holder_name")
    val accountHolderName: String,
    
    @SerializedName("account_number")
    val accountNumber: String,
    
    @SerializedName("ifsc_code")
    val ifscCode: String,
    
    @SerializedName("bank_name")
    val bankName: String? = null,
    
    @SerializedName("upi_id")
    val upiId: String? = null
)

/**
 * Request Withdrawal Response
 */
data class RequestWithdrawalResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("withdrawal")
    val withdrawal: WithdrawalDto,
    
    @SerializedName("available_balance")
    val availableBalance: Double,
    
    @SerializedName("message")
    val message: String
)

/**
 * Withdrawal History Response
 */
data class WithdrawalHistoryResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("withdrawals")
    val withdrawals: List<WithdrawalDto>,
    
    @SerializedName("pagination")
    val pagination: PaginationDto
)

/**
 * Get Bank Accounts Response
 */
data class BankAccountsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("bank_accounts")
    val bankAccounts: List<BankAccountDto>
)

/**
 * Pagination DTO
 */
data class PaginationDto(
    @SerializedName("current_page")
    val currentPage: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("total_items")
    val totalItems: Int,
    
    @SerializedName("per_page")
    val perPage: Int
)

