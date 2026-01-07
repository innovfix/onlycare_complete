package com.onlycare.app.presentation.screens.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RateUserState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null,
    val isBlocking: Boolean = false,
    val blockSuccess: Boolean = false,
    val blockError: String? = null,
    val isFemaleUser: Boolean = false
)

@HiltViewModel
class RateUserViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(RateUserState())
    val state: StateFlow<RateUserState> = _state.asStateFlow()
    
    companion object {
        private const val TAG = "RateUserViewModel"
    }
    
    init {
        // Check if current user is female
        val isFemale = sessionManager.getGender() == Gender.FEMALE
        _state.value = _state.value.copy(isFemaleUser = isFemale)
        Log.d(TAG, "Current user is ${if (isFemale) "FEMALE" else "MALE"}")
    }
    
    fun loadUser(userId: String) {
        if (userId.isEmpty()) {
            _state.value = _state.value.copy(error = "Invalid user ID")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            Log.d(TAG, "Loading user details for: $userId")
            
            repository.getUserById(userId).onSuccess { user ->
                Log.d(TAG, "✅ User loaded: ${user.name}")
                _state.value = _state.value.copy(
                    user = user,
                    isLoading = false
                )
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load user: ${error.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }
    
    fun blockUser(userId: String) {
        if (userId.isEmpty()) {
            _state.value = _state.value.copy(blockError = "Invalid user ID")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isBlocking = true,
                blockError = null,
                blockSuccess = false
            )
            
            Log.d(TAG, "🚫 Blocking user: $userId")
            
            repository.blockUser(userId).onSuccess {
                Log.d(TAG, "✅ User blocked successfully")
                _state.value = _state.value.copy(
                    isBlocking = false,
                    blockSuccess = true
                )
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to block user: ${error.message}")
                _state.value = _state.value.copy(
                    isBlocking = false,
                    blockError = error.message ?: "Failed to block user"
                )
            }
        }
    }
    
    fun clearBlockSuccess() {
        _state.value = _state.value.copy(blockSuccess = false)
    }
    
    fun clearBlockError() {
        _state.value = _state.value.copy(blockError = null)
    }

    fun submitRating(
        userId: String,
        callId: String,
        rating: Float,
        selectedTags: List<String>,
        additionalComment: String,
        shouldBlockUser: Boolean
    ) {
        if (userId.isBlank()) {
            _state.value = _state.value.copy(submitError = "Invalid user ID")
            return
        }
        if (callId.isBlank()) {
            _state.value = _state.value.copy(submitError = "Invalid call ID")
            return
        }
        if (rating <= 0f) {
            _state.value = _state.value.copy(submitError = "Please select a rating")
            return
        }

        val tags = selectedTags.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val comment = additionalComment.trim()

        val feedback = buildString {
            if (tags.isNotEmpty()) {
                append("tags=")
                append(tags.joinToString("|"))
            }
            if (comment.isNotEmpty()) {
                if (isNotEmpty()) append("; ")
                append("comment=")
                append(comment)
            }
        }.ifBlank { null }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSubmitting = true,
                submitError = null,
                submitSuccess = false
            )

            Log.d(TAG, "⭐ Submitting rating: callId=$callId rating=$rating tags=${tags.size} comment=${comment.length}")

            // If female user checked "Block this user", block first, then submit rating.
            if (_state.value.isFemaleUser && shouldBlockUser) {
                Log.d(TAG, "🚫 Blocking user on submit: $userId")
                repository.blockUser(userId).onFailure { error ->
                    Log.e(TAG, "❌ Failed to block user on submit: ${error.message}")
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        submitError = error.message ?: "Failed to block user"
                    )
                    return@launch
                }
            }

            repository.rateCall(callId, rating, feedback).onSuccess {
                Log.d(TAG, "✅ Rating submitted")
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    submitSuccess = true
                )
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to submit rating: ${error.message}")
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    submitError = error.message ?: "Failed to submit rating"
                )
            }
        }
    }

    fun clearSubmitSuccess() {
        _state.value = _state.value.copy(submitSuccess = false)
    }

    fun clearSubmitError() {
        _state.value = _state.value.copy(submitError = null)
    }
}


