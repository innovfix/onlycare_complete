package com.onlycare.app.presentation.screens.profile

import androidx.lifecycle.SavedStateHandle
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
 * ViewModel for User Profile Screen
 * Fetches user profile data from API
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val userId: String = savedStateHandle.get<String>("userId") ?: ""
    
    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    fun loadUserProfile() {
        if (userId.isEmpty()) {
            android.util.Log.e("UserProfileViewModel", "loadUserProfile: userId is empty!")
            _state.update { 
                it.copy(
                    isLoading = false,
                    error = "Invalid user ID"
                )
            }
            return
        }
        
        viewModelScope.launch {
            android.util.Log.d("UserProfileViewModel", "loadUserProfile: Loading profile for userId: $userId")
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getUserById(userId)
            
            result.onSuccess { user ->
                android.util.Log.d("UserProfileViewModel", "loadUserProfile: Success! User loaded: ${user.name}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        error = null
                    )
                }
            }.onFailure { error ->
                android.util.Log.e("UserProfileViewModel", "loadUserProfile: Error = ${error.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load user profile"
                    )
                }
            }
        }
    }
    
    fun retry() {
        loadUserProfile()
    }

    fun sendFriendRequest() {
        if (userId.isBlank()) return
        if (_state.value.isSendingFriendRequest || _state.value.friendRequestSent) return

        _state.update { it.copy(isSendingFriendRequest = true, friendRequestError = null) }
        viewModelScope.launch {
            val result = repository.sendFriendRequest(userId)
            result.onSuccess {
                _state.update {
                    it.copy(
                        isSendingFriendRequest = false,
                        friendRequestSent = true,
                        friendRequestError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSendingFriendRequest = false,
                        friendRequestSent = false,
                        friendRequestError = error.message ?: "Failed to send friend request"
                    )
                }
            }
        }
    }

    fun clearFriendRequestError() {
        _state.update { it.copy(friendRequestError = null) }
    }
}

/**
 * UI State for User Profile Screen
 */
data class UserProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val isSendingFriendRequest: Boolean = false,
    val friendRequestSent: Boolean = false,
    val friendRequestError: String? = null
)

