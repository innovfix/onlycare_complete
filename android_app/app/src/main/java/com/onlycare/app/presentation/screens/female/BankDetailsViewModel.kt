package com.onlycare.app.presentation.screens.female

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BankDetailsState(
    val upiId: String = "",
    val accountHolderName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val bankName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class BankDetailsViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BankDetailsState())
    val state: StateFlow<BankDetailsState> = _state.asStateFlow()

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
                // Pre-fill UPI ID if available
                _state.update {
                    it.copy(
                        upiId = userDto.upiId ?: ""
                    )
                }
            }.onFailure {
                // If API fails, field will remain empty
            }
        }
    }

    fun onUpiIdChange(newValue: String) {
        val cleaned = newValue.trim().replace(" ", "")
        _state.update { it.copy(upiId = cleaned, error = null) }
    }

    fun onAccountHolderNameChange(newValue: String) {
        _state.update { it.copy(accountHolderName = newValue, error = null) }
    }

    fun onAccountNumberChange(newValue: String) {
        _state.update { it.copy(accountNumber = newValue, error = null) }
    }

    fun onIfscCodeChange(newValue: String) {
        _state.update { it.copy(ifscCode = newValue.uppercase(), error = null) }
    }

    fun onBankNameChange(newValue: String) {
        _state.update { it.copy(bankName = newValue, error = null) }
    }

    fun saveUpiId() {
        val currentState = _state.value
        
        // Validate UPI ID
        if (currentState.upiId.isEmpty()) {
            _state.update { it.copy(error = "Please enter UPI ID") }
            return
        }
        
        if (!currentState.upiId.contains("@")) {
            _state.update { it.copy(error = "Invalid UPI ID format") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Call the updateUpi API for verification
            val result = repository.updateUpi(currentState.upiId)

            result.onSuccess { _ ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            }.onFailure { error ->
                val raw = error.message ?: "Failed to verify UPI ID. Please try again."
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

    fun saveBankDetails() {
        val currentState = _state.value
        
        // Validate bank details
        if (currentState.accountHolderName.isEmpty()) {
            _state.update { it.copy(error = "Please enter account holder name") }
            return
        }
        
        if (currentState.accountNumber.isEmpty()) {
            _state.update { it.copy(error = "Please enter account number") }
            return
        }
        
        if (currentState.ifscCode.isEmpty()) {
            _state.update { it.copy(error = "Please enter IFSC code") }
            return
        }
        
        if (currentState.ifscCode.length != 11) {
            _state.update { it.copy(error = "IFSC code must be 11 characters") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = repository.addBankAccount(
                accountHolderName = currentState.accountHolderName,
                accountNumber = currentState.accountNumber,
                ifscCode = currentState.ifscCode,
                bankName = currentState.bankName,
                upiId = currentState.upiId
            )

            result.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to save bank details. Please try again."
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetSuccess() {
        _state.update { it.copy(isSuccess = false) }
    }
}

