package com.onlycare.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.remote.dto.ContentSectionDto
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Refund Policy Screen
 * Fetches refund policy content from API
 */
@HiltViewModel
class RefundPolicyViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(RefundPolicyState())
    val state: StateFlow<RefundPolicyState> = _state.asStateFlow()
    
    init {
        loadRefundPolicy()
    }
    
    fun loadRefundPolicy() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getRefundPolicy()
            
            result.onSuccess { data ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        title = data.title ?: "Refund Policy",
                        lastUpdated = data.lastUpdated ?: "",
                        sections = data.content.orEmpty(),
                        htmlContent = data.htmlContent,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load refund policy"
                    )
                }
            }
        }
    }
    
    fun retry() {
        loadRefundPolicy()
    }
}

/**
 * UI State for Refund Policy Screen
 */
data class RefundPolicyState(
    val isLoading: Boolean = true,
    val title: String = "Refund Policy",
    val lastUpdated: String = "",
    val sections: List<ContentSectionDto> = emptyList(),
    val htmlContent: String? = null,
    val error: String? = null
)

