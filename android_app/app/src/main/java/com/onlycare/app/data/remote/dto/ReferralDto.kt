package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Referral Code Response
 */
data class ReferralCodeResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("referral_code")
    val referralCode: String,
    
    @SerializedName("referral_url")
    val referralUrl: String,
    
    @SerializedName("my_invites")
    val myInvites: Int,
    
    @SerializedName("per_invite_coins")
    val perInviteCoins: Int? = null, // For male users (coins)
    
    @SerializedName("per_invite_rupees")
    val perInviteRupees: Int? = null, // For female users (Rs)
    
    @SerializedName("total_coins_earned")
    val totalCoinsEarned: Int? = null, // For male users
    
    @SerializedName("total_rupees_earned")
    val totalRupeesEarned: Int? = null, // For female users
    
    @SerializedName("reward_type")
    val rewardType: String? = null, // "COINS" or "RUPEES"
    
    @SerializedName("share_message")
    val shareMessage: String,
    
    @SerializedName("whatsapp_share_url")
    val whatsappShareUrl: String
)

/**
 * Apply Referral Request
 */
data class ApplyReferralRequest(
    @SerializedName("referral_code")
    val referralCode: String
)

/**
 * Apply Referral Response
 */
data class ApplyReferralResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("bonus_coins")
    val bonusCoins: Int?,
    
    @SerializedName("referrer_bonus")
    val referrerBonus: Int?,
    
    @SerializedName("new_balance")
    val newBalance: Int?
)

/**
 * Referral History Response
 */
data class ReferralHistoryResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("referrals")
    val referrals: List<ReferralItemDto>,
    
    @SerializedName("pagination")
    val pagination: PaginationDto?
)

/**
 * Referral Item DTO
 */
data class ReferralItemDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("referred_user")
    val referredUser: ReferredUserDto,
    
    @SerializedName("bonus_coins")
    val bonusCoins: Int? = null, // For male users
    
    @SerializedName("bonus_rupees")
    val bonusRupees: Int? = null, // For female users
    
    @SerializedName("reward_type")
    val rewardType: String? = null, // "COINS" or "RUPEES"
    
    @SerializedName("is_claimed")
    val isClaimed: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("created_at_formatted")
    val createdAtFormatted: String,
    
    @SerializedName("claimed_at")
    val claimedAt: String?
)

/**
 * Referred User DTO
 */
data class ReferredUserDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("profile_image")
    val profileImage: String?
)

// Domain Model classes
data class ReferralCodeData(
    val referralCode: String,
    val referralUrl: String,
    val myInvites: Int,
    val coinsPerInvite: Int? = null, // For male users
    val rupeesPerInvite: Int? = null, // For female users
    val totalCoinsEarned: Int? = null, // For male users
    val totalRupeesEarned: Int? = null, // For female users
    val rewardType: String? = null, // "COINS" or "RUPEES"
    val shareMessage: String,
    val whatsappShareUrl: String
)

data class ReferralHistoryData(
    val totalReferrals: Int,
    val totalEarnings: Int,
    val referrals: List<ReferralItem>
)

data class ReferralItem(
    val id: String,
    val userName: String,
    val userImage: String?,
    val coinsEarned: Int? = null, // For male users
    val rupeesEarned: Int? = null, // For female users
    val rewardType: String? = null, // "COINS" or "RUPEES"
    val joinedAt: String,
    val isClaimed: Boolean
)

// Extension functions to convert DTO to Domain Model
fun ReferralCodeResponse.toDomainModel() = ReferralCodeData(
    referralCode = referralCode,
    referralUrl = referralUrl,
    myInvites = myInvites,
    coinsPerInvite = perInviteCoins,
    rupeesPerInvite = perInviteRupees,
    totalCoinsEarned = totalCoinsEarned,
    totalRupeesEarned = totalRupeesEarned,
    rewardType = rewardType,
    shareMessage = shareMessage,
    whatsappShareUrl = whatsappShareUrl
)

fun ReferralHistoryResponse.toDomainModel() = ReferralHistoryData(
    totalReferrals = referrals.size,
    totalEarnings = referrals.sumOf { it.bonusCoins ?: it.bonusRupees ?: 0 },
    referrals = referrals.map { it.toDomainModel() }
)

fun ReferralItemDto.toDomainModel() = ReferralItem(
    id = id,
    userName = referredUser.name,
    userImage = referredUser.profileImage,
    coinsEarned = bonusCoins,
    rupeesEarned = bonusRupees,
    rewardType = rewardType,
    joinedAt = createdAtFormatted,
    isClaimed = isClaimed
)

