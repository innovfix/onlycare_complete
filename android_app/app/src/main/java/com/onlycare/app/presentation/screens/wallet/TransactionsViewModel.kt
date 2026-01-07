package com.onlycare.app.presentation.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: ApiDataRepository,  // ← PRODUCTION: Real API Only
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    private fun loadTransactions() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            repository.getTransactionHistory().collect { result ->
                result.onSuccess { transactions ->
                    _state.update {
                        it.copy(
                            transactions = transactions,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load transactions"
                        )
                    }
                }
            }
        }
    }
    
    fun retry() {
        loadTransactions()
    }
    
    fun getGender(): Gender {
        return sessionManager.getGender()
    }
}



