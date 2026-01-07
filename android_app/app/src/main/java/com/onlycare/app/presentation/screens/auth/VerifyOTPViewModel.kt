package com.onlycare.app.presentation.screens.auth

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.BuildConfig
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.utils.FCMTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyOTPState(
    val otp: String = "",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isNewUser: Boolean = false,

    // Resend timer
    val resendSecondsRemaining: Int = 60,

    // Debug-only helper: backend may return OTP in response (dev/testing)
    val debugOtp: String? = null
)

@HiltViewModel
class VerifyOTPViewModel @Inject constructor(
    private val repository: ApiDataRepository,  // ← PRODUCTION: Real API Only
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val phone: String = savedStateHandle.get<String>("phone") ?: ""
    
    private val _state = MutableStateFlow(VerifyOTPState())
    val state: StateFlow<VerifyOTPState> = _state.asStateFlow()

    private var resendJob: kotlinx.coroutines.Job? = null

    init {
        startResendTimer()
    }

    private fun startResendTimer() {
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            // Reset to 60 each time timer starts
            _state.update { it.copy(resendSecondsRemaining = 60) }
            while (_state.value.resendSecondsRemaining > 0) {
                kotlinx.coroutines.delay(1000)
                _state.update { s ->
                    s.copy(resendSecondsRemaining = (s.resendSecondsRemaining - 1).coerceAtLeast(0))
                }
            }
        }
    }
    
    fun onOtpChange(otp: String) {
        // Only allow digits
        val filtered = otp.filter { it.isDigit() }.take(6)
        _state.update { it.copy(otp = filtered, otpError = null) }
    }
    
    fun onVerify() {
        val otp = _state.value.otp
        
        // Validation
        if (otp.length != 6) {
            _state.update { it.copy(otpError = "Please enter a valid 6-digit OTP") }
            return
        }
        
        _state.update { it.copy(isLoading = true, otpError = null) }
        
        viewModelScope.launch {
            // PRODUCTION: Use Real API Only
            val otpId = savedStateHandle.get<String>("otpId") ?: ""
            
            // Avoid logging OTPs in release builds (sensitive)
            if (BuildConfig.DEBUG) {
                Log.e("VerifyOTPViewModel", "Verifying OTP: $otp for Phone: $phone with ID: $otpId")
            } else {
                Log.d("VerifyOTPViewModel", "Verifying OTP for Phone: $phone with ID: $otpId")
            }
            
            if (otpId.isEmpty()) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        otpError = "Invalid session. Please login again."
                    )
                }
                return@launch
            }
            
            val result = repository.verifyOtp(phone, otp, otpId)
            
            result.onSuccess { response ->
                // Save auth token
                sessionManager.saveAuthToken(response.accessToken)
                
                // Update FCM token after successful login
                updateFCMTokenAfterLogin()
                
                if (response.userExists && response.user != null) {
                    // Existing user - has complete profile
                    val user = response.user
                    sessionManager.saveUserSession(
                        userId = user.id,
                        phone = user.phone ?: phone,
                        gender = if (user.gender == "FEMALE") com.onlycare.app.domain.model.Gender.FEMALE 
                                else com.onlycare.app.domain.model.Gender.MALE,
                        name = user.name,
                        age = user.age ?: 0,
                        coinBalance = user.coinBalance ?: 0
                    )
                    
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            isSuccess = true,
                            isNewUser = false
                        ) 
                    }
                } else {
                    // New user - needs profile setup
                    sessionManager.saveUserSession(
                        userId = "temp_${System.currentTimeMillis()}",
                        phone = phone,
                        gender = com.onlycare.app.domain.model.Gender.MALE
                    )
                    
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            isSuccess = true,
                            isNewUser = true
                        ) 
                    }
                }
            }.onFailure { error ->
                Log.e("VerifyOTPViewModel", "Verification failed: ${error.message}", error)
                val isDummyOtp = otp == "000000"
                _state.update {
                    it.copy(
                        isLoading = false,
                        otpError = if (isDummyOtp) {
                            "Dummy OTP (000000) is not enabled on backend. Please enable test OTP on server-side."
                        } else {
                            error.message ?: "Invalid OTP. Please try again."
                        }
                    )
                }
            }
        }
    }

    fun onResendOtp() {
        if (phone.isBlank()) return
        if (_state.value.resendSecondsRemaining > 0) return

        _state.update { it.copy(isLoading = true, otpError = null) }

        viewModelScope.launch {
            val result = repository.sendOtp(phone, "+91")

            result.onSuccess { response ->
                // Update otpId so Verify uses the latest one
                savedStateHandle["otpId"] = response.otpId

                _state.update {
                    it.copy(
                        isLoading = false,
                        otp = "",
                        otpError = null,
                        debugOtp = response.otp
                    )
                }

                // Restart countdown after successful resend
                startResendTimer()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        otpError = error.message ?: "Failed to resend OTP. Please try again."
                    )
                }
            }
        }
    }
    
    /**
     * Update FCM token after successful login
     * Called after auth token is saved to ensure API call is authenticated
     */
    private fun updateFCMTokenAfterLogin() {
        viewModelScope.launch {
            try {
                // Get current FCM token
                val fcmToken = FCMTokenManager.getFCMToken()
                
                if (fcmToken != null) {
                    Log.d("VerifyOTPViewModel", "Updating FCM token after login: $fcmToken")
                    val result = repository.updateFCMToken(fcmToken)
                    
                    if (result.isSuccess) {
                        Log.d("VerifyOTPViewModel", "✅ FCM token updated successfully after login")
                    } else {
                        Log.e("VerifyOTPViewModel", "❌ Failed to update FCM token: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.w("VerifyOTPViewModel", "⚠️ FCM token not available, skipping update")
                }
            } catch (e: Exception) {
                Log.e("VerifyOTPViewModel", "Error updating FCM token after login", e)
                // Don't block login flow if FCM token update fails
            }
        }
    }
}


