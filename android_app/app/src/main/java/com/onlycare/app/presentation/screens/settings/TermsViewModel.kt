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
 * ViewModel for Terms & Conditions Screen
 * Fetches terms and conditions content from API
 */
@HiltViewModel
class TermsViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TermsState())
    val state: StateFlow<TermsState> = _state.asStateFlow()
    
    init {
        loadTerms()
    }
    
    fun loadTerms() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getTermsAndConditions()
            
            result.onSuccess { data ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        title = data.title ?: "Terms & Conditions",
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
                        error = error.message ?: "Failed to load terms and conditions"
                    )
                }
            }
        }
    }
    
    fun retry() {
        loadTerms()
    }
}

/**
 * UI State for Terms & Conditions Screen
 */
data class TermsState(
    val isLoading: Boolean = true,
    val title: String = "Terms & Conditions",
    val lastUpdated: String = "",
    val sections: List<ContentSectionDto> = emptyList(),
    val htmlContent: String? = null,
    val error: String? = null
)

