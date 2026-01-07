package com.onlycare.app.presentation.screens.female

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KYCState(
    val fullName: String = "",
    val panNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val verifiedName: String? = null
)

@HiltViewModel
class KYCViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(KYCState())
    val state: StateFlow<KYCState> = _state.asStateFlow()

    private fun isRateLimitedMsg(msg: String): Boolean {
        val m = msg.lowercase()
        return m.contains("too many") || m.contains("maximum") || m.contains("limit") || m.contains("429")
    }

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val result = repository.getCurrentUserDto()
            result.onSuccess { userDto ->
                // Pre-fill PAN card data if available
                _state.update {
                    it.copy(
                        fullName = userDto.pancardName ?: "",
                        panNumber = userDto.pancardNumber ?: ""
                    )
                }
            }.onFailure {
                // If API fails, fields will remain empty
            }
        }
    }

    fun onFullNameChange(newValue: String) {
        _state.update { it.copy(fullName = newValue, error = null) }
    }

    fun onPanNumberChange(newValue: String) {
        val cleaned = newValue.filter { it.isLetterOrDigit() }.uppercase()
        _state.update { it.copy(panNumber = cleaned.take(10), error = null) }
    }

    fun verifyPanCard() {
        val currentState = _state.value
        
        // Validate inputs
        if (currentState.fullName.isEmpty()) {
            _state.update { it.copy(error = "Please enter your full name") }
            return
        }
        
        if (currentState.panNumber.isEmpty()) {
            _state.update { it.copy(error = "Please enter PAN number") }
            return
        }
        
        if (currentState.panNumber.length != 10) {
            _state.update { it.copy(error = "PAN number must be 10 characters") }
            return
        }
        
        if (!currentState.panNumber.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))) {
            _state.update { it.copy(error = "Invalid PAN number format") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Call the updatePanCard API for verification
            val result = repository.updatePanCard(
                pancardName = currentState.fullName,
                pancardNumber = currentState.panNumber
            )

            result.onSuccess { response ->
                // IMPORTANT:
                // Female referral reward is KYC/verification-gated. Backend typically credits only when
                // user becomes VERIFIED/APPROVED (admin-side), not when PAN is submitted.
                // We therefore apply referral when VerificationPending polling detects verified=true.

                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        verifiedName = response.data?.verifiedName
                    )
                }
            }.onFailure { error ->
                val raw = error.message ?: "Failed to verify PAN card. Please try again."
                val msg = if (isRateLimitedMsg(raw)) "Please wait a few seconds and try again." else raw
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = msg
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetSuccess() {
        _state.update { it.copy(isSuccess = false, verifiedName = null) }
    }
}

