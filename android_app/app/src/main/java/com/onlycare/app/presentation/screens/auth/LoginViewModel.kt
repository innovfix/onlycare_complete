package com.onlycare.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.utils.FCMTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val phone: String = "",
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val otpId: String? = null,

    // UI state used by LoginScreen
    val hasReferral: Boolean = false,
    val referralCode: String = "",
    val referralApplied: Boolean = false,
    val referralError: String? = null,
    val termsAccepted: Boolean = true,

    // Debug-only helper: backend may return OTP in response (dev/testing)
    val debugOtp: String? = null,

    // Truecaller login navigation flags (skip OTP)
    val truecallerLoginSuccess: Boolean = false,
    val truecallerIsNewUser: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: ApiDataRepository,  // ← PRODUCTION: Real API Only
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    fun onPhoneChange(phone: String) {
        // Only allow digits
        val filtered = phone.filter { it.isDigit() }.take(10)
        val startsOk = filtered.isEmpty() || filtered.first() in listOf('6', '7', '8', '9')
        _state.update {
            it.copy(
                phone = filtered,
                phoneError = if (!startsOk) "Please enter a valid number starting with 6, 7, 8, or 9" else null,
                isSuccess = false,
                otpId = null,
                debugOtp = null
            )
        }
    }

    fun onToggleReferral(enabled: Boolean) {
        _state.update {
            if (!enabled) {
                it.copy(
                    hasReferral = false,
                    referralCode = "",
                    referralApplied = false,
                    referralError = null
                )
            } else {
                it.copy(
                    hasReferral = true,
                    referralApplied = false,
                    referralError = null
                )
            }
        }
    }

    fun onReferralCodeChange(code: String) {
        val normalized = code
            .trim()
            .uppercase()
            .filter { it.isLetterOrDigit() }
            .take(8)

        _state.update {
            it.copy(
                referralCode = normalized,
                referralApplied = false,
                referralError = null
            )
        }
    }

    fun onApplyReferral() {
        val code = _state.value.referralCode
        if (code.length != 8) {
            _state.update {
                it.copy(
                    referralApplied = false,
                    referralError = "Enter a valid 8-character referral code"
                )
            }
            return
        }
        // Save referral code to session manager for use during registration
        sessionManager.saveReferralCode(code)
        _state.update { it.copy(referralApplied = true, referralError = null) }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _state.update { it.copy(termsAccepted = accepted) }
    }
    
    fun onSendOTP() {
        val s = _state.value
        val phone = s.phone
        
        // Validation
        if (phone.length != 10) {
            _state.update { it.copy(phoneError = "Please enter a valid 10-digit phone number") }
            return
        }

        // Must start with 6/7/8/9 (India mobile numbers)
        val firstDigit = phone.firstOrNull()
        if (firstDigit == null || firstDigit !in listOf('6', '7', '8', '9')) {
            _state.update { it.copy(phoneError = "Please enter a valid number starting with 6, 7, 8, or 9") }
            return
        }

        // Enforce Terms + referral rules
        if (!s.termsAccepted) {
            _state.update { it.copy(phoneError = "Please accept Terms & Conditions to continue") }
            return
        }
        if (s.hasReferral && !s.referralApplied) {
            _state.update { it.copy(phoneError = "Please apply referral code or uncheck it to continue") }
            return
        }
        
        _state.update { it.copy(isLoading = true, phoneError = null) }
        
        viewModelScope.launch {
            // PRODUCTION: Use Real API Only
            val result = repository.sendOtp(phone, "+91")
            
            result.onSuccess { response ->
                // DEBUG: Log OTP for testing (backend may return OTP in response)
                android.util.Log.e("OTP_DEBUG", "Your OTP is: ${response.otp}")
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        otpId = response.otpId,
                        debugOtp = response.otp
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        phoneError = error.message ?: "Failed to send OTP. Please try again."
                    )
                }
            }
        }
    }
    
    fun resetState() {
        _state.update { it.copy(isSuccess = false, otpId = null, debugOtp = null) }
    }

    fun resetTruecallerState() {
        _state.update { it.copy(truecallerLoginSuccess = false, truecallerIsNewUser = false) }
    }

    fun loginWithTruecaller(code: String, codeVerifier: String) {
        _state.update { it.copy(isLoading = true, phoneError = null) }

        viewModelScope.launch {
            val result = repository.loginWithTruecaller(code = code, codeVerifier = codeVerifier)

            result.onSuccess { resp ->
                val token = resp.token ?: resp.accessToken
                val user = resp.data ?: resp.user

                val isRegistered = when {
                    resp.registered != null -> resp.registered == true
                    resp.userExists != null -> resp.userExists == true
                    user != null -> true
                    else -> false
                }

                // Prefer phone from user, then fallback to userNumber
                val resolvedPhone = (user?.phone?.filter { it.isDigit() }?.takeLast(10))
                    ?: (resp.userNumber?.filter { it.isDigit() }?.takeLast(10))
                    ?: _state.value.phone

                if (isRegistered && user != null) {
                    // Existing user: save full session
                    val gender = if (user.gender == "FEMALE") Gender.FEMALE else Gender.MALE
                    sessionManager.saveUserSession(
                        userId = user.id,
                        phone = user.phone ?: resolvedPhone,
                        gender = gender,
                        name = user.name,
                        age = user.age ?: 0,
                        coinBalance = user.coinBalance ?: 0
                    )

                    // Update FCM token only if we have an auth token
                    if (!token.isNullOrBlank()) {
                        updateFCMTokenAfterLogin()
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            truecallerLoginSuccess = true,
                            truecallerIsNewUser = false
                        )
                    }
                } else {
                    // New user (not registered): move to registration flow
                    sessionManager.saveUserSession(
                        userId = "temp_${System.currentTimeMillis()}",
                        phone = resolvedPhone,
                        gender = Gender.MALE
                    )

                    _state.update {
                        it.copy(
                            isLoading = false,
                            truecallerLoginSuccess = true,
                            truecallerIsNewUser = true
                        )
                    }
                }
            }.onFailure { error ->
                Log.e("LoginViewModel", "Truecaller login failed: ${error.message}", error)
                _state.update {
                    it.copy(
                        isLoading = false,
                        phoneError = error.message ?: "Truecaller login failed. Please try OTP.",
                        truecallerLoginSuccess = false
                    )
                }
            }
        }
    }

    private fun updateFCMTokenAfterLogin() {
        viewModelScope.launch {
            try {
                val authToken = sessionManager.getAuthToken()
                if (authToken.isNullOrBlank()) return@launch

                val fcmToken = FCMTokenManager.getFCMToken()
                if (fcmToken != null) {
                    repository.updateFCMToken(fcmToken)
                }
            } catch (_: Exception) {
                // Don't block login flow if FCM token update fails
            }
        }
    }
}



