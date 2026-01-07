package com.onlycare.app.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.FriendRequest
import com.onlycare.app.domain.model.FriendsData
import com.onlycare.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsState(
    val friends: List<User> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val receivedRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for Friends Screen
 * Manages friends list, friend requests, and friend-related actions
 */
@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FriendsState())
    val state: StateFlow<FriendsState> = _state.asStateFlow()

    init {
        loadFriends()
    }

    /**
     * Load friends list from API
     */
    fun loadFriends() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getFriends().collect { result ->
                result.onSuccess { data: FriendsData ->
                    _state.update {
                        it.copy(
                            friends = data.friends,
                            sentRequests = data.sentRequests,
                            receivedRequests = data.receivedRequests,
                            isLoading = false,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load friends"
                        )
                    }
                }
            }
        }
    }

    /**
     * Send friend request
     */
    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.sendFriendRequest(userId)

            result.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Friend request sent!",
                        error = null
                    )
                }
                // Reload friends to update the list
                loadFriends()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send friend request"
                    )
                }
            }
        }
    }

    /**
     * Accept friend request
     */
    fun acceptFriendRequest(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.acceptFriendRequest(userId)

            result.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Friend request accepted!",
                        error = null
                    )
                }
                // Reload friends to update the list
                loadFriends()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to accept friend request"
                    )
                }
            }
        }
    }

    /**
     * Reject friend request
     */
    fun rejectFriendRequest(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.rejectFriendRequest(userId)

            result.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Friend request rejected",
                        error = null
                    )
                }
                // Reload friends to update the list
                loadFriends()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to reject friend request"
                    )
                }
            }
        }
    }

    /**
     * Remove friend
     */
    fun removeFriend(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.removeFriend(userId)

            result.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Friend removed",
                        error = null
                    )
                }
                // Reload friends to update the list
                loadFriends()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to remove friend"
                    )
                }
            }
        }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Retry loading friends
     */
    fun retry() {
        loadFriends()
    }
}

