package com.onlycare.app.presentation.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.CoinPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentState(
    val selectedPackage: CoinPackage? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    fun loadPackage(packageId: String) {
        if (packageId.isBlank()) {
            _state.update { it.copy(error = "Invalid package", selectedPackage = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, selectedPackage = null) }

            try {
                val result = repository.getCoinPackages().first()
                result.onSuccess { packages ->
                    val pkg = packages.firstOrNull { it.id == packageId }
                    if (pkg == null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Selected package not found",
                                selectedPackage = null
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                selectedPackage = pkg
                            )
                        }
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load package",
                            selectedPackage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load package",
                        selectedPackage = null
                    )
                }
            }
        }
    }
}

