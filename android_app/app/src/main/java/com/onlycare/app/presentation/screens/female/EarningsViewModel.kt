package com.onlycare.app.presentation.screens.female

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Earnings Screen
 * Fetches real earnings data from API
 */
@HiltViewModel
class EarningsViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(EarningsState())
    val state: StateFlow<EarningsState> = _state.asStateFlow()

    private var transactionsJob: Job? = null
    
    init {
        loadEarnings()
        loadRecentTransactions()
    }
    
    fun loadEarnings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getEarningsDashboard()
            
            result.onSuccess { dashboard ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        currentBalance = dashboard.availableBalance,
                        totalEarnings = dashboard.totalEarnings,
                        thisMonthEarnings = dashboard.monthEarnings,
                        totalCalls = dashboard.totalCalls,
                        avgRating = 0.0, // Rating not available in dashboard
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load earnings"
                    )
                }
            }
        }
    }
    
    fun loadRecentTransactions() {
        transactionsJob?.cancel()
        transactionsJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingTransactions = true) }
            
            repository.getTransactionHistory().collect { result ->
                result.onSuccess { transactions ->
                    _state.update {
                        it.copy(
                            recentTransactions = transactions,
                            isLoadingTransactions = false
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(
                            recentTransactions = emptyList(),
                            isLoadingTransactions = false
                        )
                    }
                }
            }
        }
    }

    fun refreshFast() {
        // Trigger a quick refresh when returning to this screen (e.g., after withdrawal)
        loadEarnings()
        loadRecentTransactions()
    }
    
    fun retry() {
        loadEarnings()
        loadRecentTransactions()
    }
}

/**
 * UI State for Earnings Screen
 */
data class EarningsState(
    val isLoading: Boolean = true,
    val currentBalance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val thisMonthEarnings: Double = 0.0,
    val totalCalls: Int = 0,
    val avgRating: Double = 0.0,
    val error: String? = null,
    val recentTransactions: List<com.onlycare.app.domain.model.Transaction> = emptyList(),
    val isLoadingTransactions: Boolean = false
)

