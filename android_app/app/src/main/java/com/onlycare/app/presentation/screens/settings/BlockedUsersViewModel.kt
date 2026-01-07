package com.onlycare.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Blocked Users Screen
 * Manages blocked users list and unblock actions
 */
@HiltViewModel
class BlockedUsersViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(BlockedUsersState())
    val state: StateFlow<BlockedUsersState> = _state.asStateFlow()
    
    init {
        loadBlockedUsers()
    }
    
    fun loadBlockedUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getBlockedUsers()
            
            result.onSuccess { users ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        blockedUsers = users,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load blocked users"
                    )
                }
            }
        }
    }
    
    fun unblockUser(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUnblocking = true) }
            
            val result = repository.unblockUser(userId)
            
            result.onSuccess {
                // Remove user from list
                _state.update { state ->
                    state.copy(
                        isUnblocking = false,
                        blockedUsers = state.blockedUsers.filter { it.id != userId },
                        successMessage = "User unblocked successfully"
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isUnblocking = false,
                        error = error.message ?: "Failed to unblock user"
                    )
                }
            }
        }
    }
    
    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
    
    fun retry() {
        loadBlockedUsers()
    }
}

/**
 * UI State for Blocked Users Screen
 */
data class BlockedUsersState(
    val isLoading: Boolean = true,
    val isUnblocking: Boolean = false,
    val blockedUsers: List<User> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

