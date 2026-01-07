package com.onlycare.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReferEarnState(
    val referralCode: String? = null,
    val shareMessage: String? = null,
    val myInvites: Int = 0,
    val coinsPerInvite: Int? = null, // For male users
    val rupeesPerInvite: Int? = null, // For female users
    val totalCoinsEarned: Int? = null, // For male users
    val totalRupeesEarned: Int? = null, // For female users
    val rewardType: String? = null, // "COINS" or "RUPEES"
    val referralHistory: List<ReferralHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val copySuccess: Boolean = false
)

data class ReferralHistory(
    val userName: String,
    val joinedDate: String,
    val coinsEarned: Int? = null, // For male users
    val rupeesEarned: Int? = null, // For female users
    val rewardType: String? = null // "COINS" or "RUPEES"
)

@HiltViewModel
class ReferEarnViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ReferEarnState())
    val state: StateFlow<ReferEarnState> = _state.asStateFlow()
    
    val isFemale: Boolean
        get() = sessionManager.getGender() == Gender.FEMALE

    init {
        loadReferralData()
    }

    fun refresh() {
        loadReferralData()
    }

    private fun loadReferralData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val isFemaleUser = isFemale

            // Get referral code (generates unique code if user doesn't have one)
            val codeResult = repository.getReferralCode()
            codeResult.onSuccess { data ->
                Log.d("ReferEarnVM", "getReferralCode: myInvites=${data.myInvites}, rewardType=${data.rewardType}, totalCoins=${data.totalCoinsEarned}, totalRupees=${data.totalRupeesEarned}, coinsPerInvite=${data.coinsPerInvite}, rupeesPerInvite=${data.rupeesPerInvite}")
                _state.update {
                    it.copy(
                        referralCode = data.referralCode,
                        shareMessage = data.shareMessage,
                        myInvites = data.myInvites,
                        coinsPerInvite = data.coinsPerInvite,
                        rupeesPerInvite = data.rupeesPerInvite,
                        rewardType = data.rewardType,
                        // Also set totals from code endpoint so UI isn't 0 while history loads / if history is empty
                        totalCoinsEarned = if (!isFemaleUser) data.totalCoinsEarned else it.totalCoinsEarned,
                        totalRupeesEarned = if (isFemaleUser) data.totalRupeesEarned else it.totalRupeesEarned
                    )
                }
            }.onFailure { error ->
                Log.e("ReferEarnVM", "getReferralCode failed: ${error.message}")
                _state.update { it.copy(error = error.message) }
            }

            // Get referral history
            val historyResult = repository.getReferralHistory()
            historyResult.onSuccess { data ->
                Log.d("ReferEarnVM", "getReferralHistory: totalReferrals=${data.totalReferrals}, totalEarnings=${data.totalEarnings}, items=${data.referrals.size}")
                _state.update {
                    val normalizedHistory = data.referrals.map { ref ->
                        // Fallback: some backends may still send 50 in `bonus_coins` even for rupees.
                        // For female view, treat it as rupees if rupeesEarned is missing.
                        val normalizedRupees = if (isFemaleUser) {
                            ref.rupeesEarned ?: ref.coinsEarned
                        } else {
                            ref.rupeesEarned
                        }
                        val normalizedCoins = if (!isFemaleUser) {
                            ref.coinsEarned ?: ref.rupeesEarned
                        } else {
                            ref.coinsEarned
                        }
                        ReferralHistory(
                            userName = ref.userName,
                            joinedDate = ref.joinedAt,
                            coinsEarned = normalizedCoins,
                            rupeesEarned = normalizedRupees,
                            rewardType = ref.rewardType
                        )
                    }
                    it.copy(
                        myInvites = data.totalReferrals,
                        // Important: totalEarnings is "sum of rewards" regardless of type; route it by gender.
                        totalCoinsEarned = if (!isFemaleUser) data.totalEarnings else it.totalCoinsEarned,
                        totalRupeesEarned = if (isFemaleUser) data.totalEarnings else it.totalRupeesEarned,
                        referralHistory = normalizedHistory,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                Log.e("ReferEarnVM", "getReferralHistory failed: ${error.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun onCopyCode() {
        _state.update { it.copy(copySuccess = true) }
        // Reset after showing toast
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _state.update { it.copy(copySuccess = false) }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

