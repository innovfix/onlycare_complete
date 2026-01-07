package com.onlycare.app.presentation.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificationPendingState(
    val isVerified: Boolean = false,
    val isChecking: Boolean = true
)

@HiltViewModel
class VerificationPendingViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        private const val TAG = "VerificationPendingVM"
        private const val POLL_INTERVAL_MS = 10_000L
    }

    private val _state = MutableStateFlow(
        VerificationPendingState(
            isVerified = sessionManager.isVerified(),
            isChecking = true
        )
    )
    val state: StateFlow<VerificationPendingState> = _state.asStateFlow()

    init {
        startPollingVerification()
    }

    private fun startPollingVerification() {
        // If already verified locally, stop immediately
        if (sessionManager.isVerified()) {
            _state.update { it.copy(isVerified = true, isChecking = false) }
            return
        }

        viewModelScope.launch {
            while (true) {
                try {
                    _state.update { it.copy(isChecking = true) }

                    val result = repository.getCurrentUserDto()
                    result.onSuccess { dto ->
                        val verifiedValue = dto.isVerified
                        val verified = isVerifiedFromDto(verifiedValue, dto.verifiedDatetime, dto.kycStatus)
                        Log.d(TAG, "Polled users/me: isVerified=$verified")

                        if (verified) {
                            // Female→Female referral reward should be applied ONLY when user is actually verified.
                            // If this user registered with a referral code, apply it now so backend credits referrer (₹50).
                            val referralCode = sessionManager.getReferralCode()
                            if (!referralCode.isNullOrBlank()) {
                                repository.applyReferralCode(referralCode)
                                    .onSuccess { resp ->
                                        Log.d(TAG, "Referral applied after verification: ${resp.message}")
                                        // If backend returns new balance for coin-based flows, update it (safe no-op for females).
                                        resp.newBalance?.let { sessionManager.updateCoinBalance(it) }
                                        sessionManager.clearReferralCode()
                                    }
                                    .onFailure { e ->
                                        // Don't block verified state; keep referral code so user can retry later (e.g., open Refer & Earn)
                                        Log.e(TAG, "Referral apply after verification failed: ${e.message}")
                                    }
                            }

                            // Persist locally so app restarts land in Main flow
                            sessionManager.updateUserProfile(isVerified = true)
                            _state.update { it.copy(isVerified = true, isChecking = false) }
                            return@launch
                        }
                    }.onFailure { e ->
                        Log.w(TAG, "Polling users/me failed: ${e.message}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Polling exception: ${e.message}")
                }

                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun isVerifiedFromDto(
        isVerifiedValue: Any?,
        verifiedDatetime: String?,
        kycStatus: String?
    ): Boolean {
        // Timestamp-based verification: if backend sets verified_datetime/verified_at, treat as verified.
        if (!verifiedDatetime.isNullOrBlank()) return true

        // Some backends reflect verification in kyc_status.
        val kyc = kycStatus?.lowercase()?.trim()
        if (kyc == "verified" || kyc == "approved" || kyc == "success") return true

        return when (isVerifiedValue) {
            is Boolean -> isVerifiedValue
            is Number -> isVerifiedValue.toInt() == 1
            is String -> {
                val v = isVerifiedValue.lowercase().trim()
                v == "1" ||
                    v == "true" ||
                    v == "yes" ||
                    v == "on" ||
                    v == "enabled" ||
                    v == "verified" ||
                    v == "approved" ||
                    v == "success"
            }
            else -> false
        }
    }
}


