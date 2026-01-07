package com.onlycare.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.remote.dto.AvatarDto
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectGenderState(
    val maleAvatars: List<AvatarDto> = emptyList(),
    val femaleAvatars: List<AvatarDto> = emptyList(),
    val isLoadingAvatars: Boolean = false,
    val avatarError: String? = null
)

@HiltViewModel
class SelectGenderViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: ApiDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SelectGenderState())
    val state: StateFlow<SelectGenderState> = _state.asStateFlow()

    init {
        // Load both male and female avatars as soon as ViewModel is created
        loadAllAvatars()
    }

    private fun loadAllAvatars() {
        _state.update { it.copy(isLoadingAvatars = true, avatarError = null) }
        
        viewModelScope.launch {
            // Fetch both male and female avatars in parallel
            val maleDeferred = async { repository.getAvatars(Gender.MALE) }
            val femaleDeferred = async { repository.getAvatars(Gender.FEMALE) }
            
            val maleResult = maleDeferred.await()
            val femaleResult = femaleDeferred.await()
            
            _state.update {
                it.copy(
                    maleAvatars = maleResult.getOrNull() ?: emptyList(),
                    femaleAvatars = femaleResult.getOrNull() ?: emptyList(),
                    isLoadingAvatars = false,
                    avatarError = when {
                        maleResult.isFailure && femaleResult.isFailure -> 
                            "Failed to load avatars"
                        maleResult.isFailure -> 
                            "Failed to load male avatars"
                        femaleResult.isFailure -> 
                            "Failed to load female avatars"
                        else -> null
                    }
                )
            }
        }
    }

    fun saveGenderAndContinue(gender: Gender, avatarId: String) {
        // IMPORTANT:
        // During onboarding (new users), VerifyOTPViewModel stores a temp_* userId.
        // We must NOT replace it here; doing so makes the app think registration is complete
        // and can cause navigation to jump straight to Main.
        //
        // Only persist the selected gender + avatar into the existing session.
        sessionManager.updateUserProfile(
            gender = gender.name,
            profileImage = avatarId // Stores avatar ID (not URL)
        )
    }
    
    /**
     * Clear all user session data and temporary information
     * Called when user presses back button to cancel registration
     */
    fun clearSessionAndCancelRegistration() {
        // Clear all session data including auth token
        sessionManager.logout()
    }
}


