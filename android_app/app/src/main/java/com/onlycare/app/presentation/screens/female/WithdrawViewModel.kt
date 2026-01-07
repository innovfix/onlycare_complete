package com.onlycare.app.presentation.screens.female

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.remote.dto.BankAccountDto
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Withdraw Screen
 */
@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(WithdrawState())
    val state: StateFlow<WithdrawState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        loadEarningsDashboard()
        loadBankAccounts()
        refreshVerificationStatus()
    }

    fun refreshFast() {
        // Quick refresh when returning to this screen (balance / pending / KYC / UPI)
        loadEarningsDashboard()
        refreshVerificationStatus()
    }
    
    fun refreshVerificationStatus() {
        viewModelScope.launch {
            val result = repository.getCurrentUserDto()
            result.onSuccess { userDto ->
                _state.update {
                    it.copy(
                        hasKycData = !userDto.pancardName.isNullOrEmpty() && !userDto.pancardNumber.isNullOrEmpty(),
                        hasUpiData = !userDto.upiId.isNullOrEmpty()
                    )
                }
            }.onFailure {
                // If API fails, keep defaults (false)
            }
        }
    }
    
    private fun loadEarningsDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = repository.getEarningsDashboard()
            
            result.onSuccess { dashboard ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        availableBalance = dashboard.availableBalance,
                        pendingWithdrawals = dashboard.pendingWithdrawals,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load balance"
                    )
                }
            }
        }
    }
    
    private fun loadBankAccounts() {
        viewModelScope.launch {
            val result = repository.getBankAccounts()
            
            result.onSuccess { accounts ->
                _state.update {
                    it.copy(
                        bankAccounts = accounts,
                        selectedBankAccount = accounts.firstOrNull { it.isPrimary } ?: accounts.firstOrNull()
                    )
                }
            }.onFailure { error ->
                // Bank accounts failure is not critical, just log
                _state.update {
                    it.copy(bankAccountsError = error.message)
                }
            }
        }
    }
    
    fun onAmountChange(amount: String) {
        // Validate numeric input
        if (amount.isEmpty() || amount.toDoubleOrNull() != null) {
            _state.update { it.copy(amount = amount, withdrawalError = null) }
        }
    }
    
    fun selectBankAccount(account: BankAccountDto) {
        _state.update { it.copy(selectedBankAccount = account) }
    }
    
    fun requestWithdrawal() {
        val currentState = _state.value
        val amount = currentState.amount.toDoubleOrNull()
        
        // Validation
        if (amount == null || amount <= 0) {
            _state.update { it.copy(withdrawalError = "Please enter a valid amount") }
            return
        }
        
        if (amount > currentState.availableBalance) {
            _state.update { it.copy(withdrawalError = "Insufficient balance") }
            return
        }
        
        val selectedAccount = currentState.selectedBankAccount
        if (selectedAccount == null) {
            _state.update { it.copy(withdrawalError = "Please select a bank account") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, withdrawalError = null) }
            
            val result = repository.requestWithdrawal(amount, selectedAccount.id)
            
            result.onSuccess { response ->
                _state.update {
                    it.copy(
                        isProcessing = false,
                        withdrawalSuccess = true,
                        availableBalance = response.availableBalance,
                        amount = "",
                        successMessage = response.message
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isProcessing = false,
                        withdrawalError = error.message ?: "Failed to process withdrawal"
                    )
                }
            }
        }
    }
    
    fun clearWithdrawalSuccess() {
        _state.update { it.copy(withdrawalSuccess = false, successMessage = null) }
    }
    
    fun retry() {
        _state.update { it.copy(error = null, withdrawalError = null) }
        loadData()
    }
    
    fun getAmountToReceive(): Double {
        val amount = _state.value.amount.toDoubleOrNull() ?: 0.0
        val transactionFee = 0.0 // Get from settings if needed
        val tdsDeduction = amount * 0.01 // 1% TDS
        return amount - transactionFee - tdsDeduction
    }
    
    fun getTransactionFee(): Double {
        return 0.0 // Get from settings API if needed
    }
    
    fun getTdsDeduction(): Double {
        val amount = _state.value.amount.toDoubleOrNull() ?: 0.0
        return amount * 0.01 // 1% TDS
    }
}

/**
 * UI State for Withdraw Screen
 */
data class WithdrawState(
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val availableBalance: Double = 0.0,
    val pendingWithdrawals: Double = 0.0,
    val amount: String = "",
    val bankAccounts: List<BankAccountDto> = emptyList(),
    val selectedBankAccount: BankAccountDto? = null,
    val withdrawalSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val withdrawalError: String? = null,
    val bankAccountsError: String? = null,
    val hasKycData: Boolean = false,
    val hasUpiData: Boolean = false
)

