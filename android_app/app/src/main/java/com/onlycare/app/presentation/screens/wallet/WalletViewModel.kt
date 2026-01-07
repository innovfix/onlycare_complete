package com.onlycare.app.presentation.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.CoinPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletState(
    val packages: List<CoinPackage> = emptyList(),
    val currentBalance: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: ApiDataRepository  // ← PRODUCTION: Real API Only
) : ViewModel() {
    
    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()
    
    init {
        loadWalletData()
    }
    
    private fun loadWalletData() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            // Load wallet balance from dedicated endpoint
            android.util.Log.d("WalletViewModel", "loadWalletData: Fetching balance from API...")
            val balanceResult = repository.getWalletBalance()
            balanceResult.onSuccess { balance ->
                android.util.Log.d("WalletViewModel", "loadWalletData: Balance received = $balance")
                _state.update { it.copy(currentBalance = balance) }
            }.onFailure { error ->
                android.util.Log.e("WalletViewModel", "loadWalletData: Error = ${error.message}")
                _state.update {
                    it.copy(
                        error = error.message ?: "Failed to load balance"
                    )
                }
            }
            
            // Load coin packages
            android.util.Log.d("WalletViewModel", "loadWalletData: Fetching packages from API...")
            repository.getCoinPackages().collect { result ->
                result.onSuccess { packages ->
                    android.util.Log.d("WalletViewModel", "loadWalletData: Packages received = ${packages.size}")
                    if (packages.isEmpty()) {
                        android.util.Log.w("WalletViewModel", "loadWalletData: WARNING - No packages received from API!")
                    } else {
                        packages.forEachIndexed { index, pkg ->
                            android.util.Log.d("WalletViewModel", "Package $index: ${pkg.coins} coins, ₹${pkg.price}")
                        }
                    }
                    _state.update {
                        it.copy(
                            packages = packages,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    android.util.Log.e("WalletViewModel", "loadWalletData: Packages error = ${error.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load packages"
                        )
                    }
                }
            }
        }
    }
    
    fun retry() {
        loadWalletData()
    }
}



