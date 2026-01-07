package com.onlycare.app.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.ChatConversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListState(
    val conversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val repository: ApiDataRepository  // ← PRODUCTION: Real API Only
) : ViewModel() {
    
    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()
    
    init {
        loadConversations()
    }
    
    private fun loadConversations() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            repository.getConversations().collect { result ->
                result.onSuccess { conversations ->
                    _state.update {
                        it.copy(
                            conversations = conversations,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load conversations"
                        )
                    }
                }
            }
        }
    }
}



