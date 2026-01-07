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
 * ViewModel for Privacy Policy Screen
 * Fetches privacy policy content from API
 */
@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(PrivacyPolicyState())
    val state: StateFlow<PrivacyPolicyState> = _state.asStateFlow()
    
    init {
        loadPrivacyPolicy()
    }
    
    fun loadPrivacyPolicy() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getPrivacyPolicy()
            
            result.onSuccess { data ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        title = data.title ?: "Privacy Policy",
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
                        error = error.message ?: "Failed to load privacy policy"
                    )
                }
            }
        }
    }
    
    fun retry() {
        loadPrivacyPolicy()
    }
}

/**
 * UI State for Privacy Policy Screen
 */
data class PrivacyPolicyState(
    val isLoading: Boolean = true,
    val title: String = "Privacy Policy",
    val lastUpdated: String = "",
    val sections: List<ContentSectionDto> = emptyList(),
    val htmlContent: String? = null,
    val error: String? = null
)

