package com.onlycare.app.presentation.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: ApiDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    companion object {
        private const val TAG = "profilePageLog"
    }

    init {
        Log.d(TAG, "ProfileViewModel initialized - Loading user profile")
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            Log.d(TAG, "loadUserProfile() called - Starting API call to users/me")
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getCurrentUser()
            
            result.onSuccess { user ->
                Log.d(TAG, "✅ API call successful - users/me response received")
                Log.d(TAG, "User ID: ${user.id}")
                Log.d(TAG, "User Name: ${user.name}")
                Log.d(TAG, "Profile Image URL: ${user.profileImage ?: "null"}")
                Log.d(TAG, "Username: ${user.username ?: "null"}")
                Log.d(TAG, "Age: ${user.age}")
                Log.d(TAG, "Bio: ${user.bio ?: "null"}")
                
                // Update SessionManager with latest data
                sessionManager.updateUserProfile(
                    name = user.name,
                    username = user.username,
                    age = user.age,
                    bio = user.bio,
                    profileImage = user.profileImage
                )
                
                Log.d(TAG, "SessionManager updated with profile data")
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        error = null
                    )
                }
                
                Log.d(TAG, "State updated - Profile image will be displayed: ${user.profileImage != null}")
            }.onFailure { error ->
                Log.e(TAG, "❌ API call failed - Error: ${error.message}")
                Log.d(TAG, "Falling back to SessionManager cached data")
                
                // Fallback to SessionManager data if API fails
                val cachedProfileImage = sessionManager.getProfileImage()
                Log.d(TAG, "Cached Profile Image URL: ${cachedProfileImage ?: "null"}")
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = User(
                            id = sessionManager.getUserId(),
                            name = sessionManager.getName().ifEmpty { "User" },
                            username = sessionManager.getUsername(),
                            age = sessionManager.getAge(),
                            gender = sessionManager.getGender(),
                            profileImage = sessionManager.getProfileImage(),
                            bio = sessionManager.getBio(),
                            coinBalance = sessionManager.getCoinBalance(),
                            isOnline = false,
                            audioCallEnabled = false,
                            videoCallEnabled = false
                        ),
                        error = error.message
                    )
                }
            }
        }
    }

    fun retry() {
        Log.d(TAG, "retry() called - Reloading profile")
        loadUserProfile()
    }
    
    fun refresh() {
        Log.d(TAG, "refresh() called - Refreshing profile data")
        loadUserProfile()
    }

    fun logout() {
        sessionManager.logout()
    }
}

data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null
)


