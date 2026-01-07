package com.onlycare.app.presentation.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val recipientName: String = "",
    val recipientIsOnline: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ApiDataRepository,  // ← PRODUCTION: Real API Only
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val userId: String = savedStateHandle.get<String>("userId") ?: ""
    
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    
    init {
        loadMessages()
    }
    
    private fun loadMessages() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            repository.getMessages(userId).collect { result ->
                result.onSuccess { messages ->
                    _state.update {
                        it.copy(
                            messages = messages,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load messages"
                        )
                    }
                }
            }
        }
    }
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            val result = repository.sendMessage(userId, content)
            
            result.onSuccess { message ->
                // Add the new message to the list
                _state.update {
                    it.copy(messages = it.messages + message)
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(error = error.message ?: "Failed to send message")
                }
            }
        }
    }
}

