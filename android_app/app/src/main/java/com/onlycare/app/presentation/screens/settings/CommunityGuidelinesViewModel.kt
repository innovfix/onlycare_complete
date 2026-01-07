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
 * ViewModel for Community Guidelines Screen
 * Reuses Privacy Policy content from API, but presented under Community Guidelines & Moderation Policy.
 */
@HiltViewModel
class CommunityGuidelinesViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(CommunityGuidelinesState())
    val state: StateFlow<CommunityGuidelinesState> = _state.asStateFlow()
    
    init {
        loadCommunityGuidelines()
    }
    
    fun loadCommunityGuidelines() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getCommunityGuidelines()
            
            result.onSuccess { data ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        title = "Community Guidelines & Moderation Policy",
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
                        error = error.message ?: "Failed to load community guidelines"
                    )
                }
            }
        }
    }
    
    fun retry() {
        loadCommunityGuidelines()
    }
}

/**
 * UI State for Community Guidelines Screen
 */
data class CommunityGuidelinesState(
    val isLoading: Boolean = true,
    val title: String = "Community Guidelines & Moderation Policy",
    val lastUpdated: String = "",
    val sections: List<ContentSectionDto> = emptyList(),
    val htmlContent: String? = null,
    val error: String? = null
)

