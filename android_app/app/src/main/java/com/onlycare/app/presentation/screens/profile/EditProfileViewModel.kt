package com.onlycare.app.presentation.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.remote.dto.AvatarDto
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Interest
import com.onlycare.app.domain.model.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val username: String = "",
    val usernameError: String? = null,
    val selectedGender: Gender = Gender.FEMALE,
    val selectedLanguage: Language = Language.ENGLISH,
    val selectedInterests: Set<Interest> = emptySet(),
    val selectedAvatarId: String = "",
    val name: String = "",
    val age: Int? = null,
    val bio: String = "",
    val isLoading: Boolean = false,
    val isLoadingUserData: Boolean = false,
    val isLoadingAvatars: Boolean = false,
    val avatars: List<AvatarDto> = emptyList(),
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()
    
    companion object {
        private const val TAG = "EditProfileViewModel"
    }

    init {
        Log.d(TAG, "EditProfileViewModel initialized - Loading user data from API")
        // Load current user data from API
        loadCurrentUserFromAPI()
    }
    
    fun loadAvatars(gender: Gender) {
        Log.d(TAG, "loadAvatars called - Gender: $gender")
        _state.update { it.copy(isLoadingAvatars = true) }
        
        viewModelScope.launch {
            val result = repository.getAvatars(gender)
            
            result.onSuccess { avatars ->
                Log.d(TAG, "Avatars loaded successfully - Count: ${avatars.size}")
                _state.update {
                    it.copy(
                        avatars = avatars,
                        isLoadingAvatars = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to load avatars", error)
                _state.update {
                    it.copy(
                        isLoadingAvatars = false,
                        error = "Failed to load avatars: ${error.message}"
                    )
                }
            }
        }
    }

    private fun loadUserData() {
        _state.update {
            it.copy(
                username = sessionManager.getUsername().ifEmpty { "" },
                selectedGender = sessionManager.getGender(),
                selectedLanguage = sessionManager.getLanguage()
            )
        }
    }
    
    private fun loadCurrentUserFromAPI() {
        Log.d(TAG, "loadCurrentUserFromAPI called - Fetching user data from API")
        _state.update { it.copy(isLoadingUserData = true) }
        
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            
            result.onSuccess { user ->
                Log.d(TAG, "User data loaded successfully from API")
                Log.d(TAG, "User ID: ${user.id}")
                Log.d(TAG, "User Name: ${user.name}")
                Log.d(TAG, "User Username: ${user.username}")
                Log.d(TAG, "User Age: ${user.age}")
                Log.d(TAG, "User Bio: ${user.bio}")
                Log.d(TAG, "User Gender: ${user.gender}")
                Log.d(TAG, "User Language: ${user.language}")
                Log.d(TAG, "User Profile Image: ${user.profileImage}")
                Log.d(TAG, "User Interests: ${user.interests}")
                
                // Map user interests from strings to Interest enum
                val userInterests = user.interests.mapNotNull { interestName ->
                    try {
                        Interest.valueOf(interestName.uppercase())
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                }.toSet()
                
                val usernameToShow = if (user.name.isNotEmpty()) user.name else (user.username ?: "")
                Log.d(TAG, "Username field will show: $usernameToShow (from name: ${user.name.isNotEmpty()})")
                
                _state.update {
                    it.copy(
                        // Use name field if not empty, otherwise use username
                        username = usernameToShow,
                        name = user.name,
                        age = user.age,
                        bio = user.bio,
                        selectedGender = user.gender,
                        selectedLanguage = user.language,
                        selectedInterests = userInterests,
                        selectedAvatarId = user.profileImage,
                        isLoadingUserData = false
                    )
                }
                
                Log.d(TAG, "State updated with user data")
                
                // Load avatars for the user's gender
                loadAvatars(user.gender)
            }.onFailure { error ->
                Log.e(TAG, "Failed to load user data from API", error)
                Log.d(TAG, "Falling back to session data")
                // Fall back to session data if API fails
                loadUserData()
                _state.update { it.copy(isLoadingUserData = false) }
            }
        }
    }

    fun onUsernameChange(newValue: String) {
        Log.d(TAG, "onUsernameChange called - New value: $newValue")
        // Allow letters, numbers and spaces for names, up to 20 chars
        val filtered = newValue.filter { it.isLetterOrDigit() || it == ' ' }.take(20)
        val error = validateUsername(filtered)
        Log.d(TAG, "Name filtered to: $filtered, Error: $error")
        _state.update { it.copy(username = filtered, usernameError = error) }
    }

    fun onInterestToggle(interest: Interest) {
        _state.update {
            val newInterests = if (it.selectedInterests.contains(interest)) {
                it.selectedInterests - interest
            } else {
                it.selectedInterests + interest
            }
            it.copy(selectedInterests = newInterests)
        }
    }

    fun onAvatarSelected(avatarId: String) {
        _state.update { it.copy(selectedAvatarId = avatarId) }
    }

    private fun validateUsername(value: String): String? {
        return when {
            value.isEmpty() -> null
            value.length < 3 -> "Name must be at least 3 characters"
            value.length > 20 -> "Name must be 20 characters or less"
            else -> null
        }
    }

    fun saveProfile() {
        Log.d(TAG, "saveProfile called - Starting profile update")
        val currentState = _state.value
        
        Log.d(TAG, "Current state:")
        Log.d(TAG, "  - Username: ${currentState.username}")
        Log.d(TAG, "  - Selected Interests: ${currentState.selectedInterests.map { it.name }}")
        Log.d(TAG, "  - Selected Avatar ID: ${currentState.selectedAvatarId}")
        
        // Validate username
        if (currentState.username.isNotEmpty()) {
            val usernameValidation = validateUsername(currentState.username)
            if (usernameValidation != null) {
                Log.w(TAG, "Username validation failed: $usernameValidation")
                _state.update { it.copy(usernameError = usernameValidation) }
                return
            }
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val interestsList = if (currentState.selectedInterests.isNotEmpty()) {
                currentState.selectedInterests.map { it.name }
            } else null
            
            // Username value is passed in the 'name' field
            val nameToSend = currentState.username.ifEmpty { null }
            val avatarToSend = currentState.selectedAvatarId.ifEmpty { null }
            
            Log.d(TAG, "Sending update profile request:")
            Log.d(TAG, "  - name: $nameToSend (username value)")
            Log.d(TAG, "  - username: null (not used)")
            Log.d(TAG, "  - Interests: $interestsList")
            Log.d(TAG, "  - profile_image: $avatarToSend")
            
            val result = repository.updateProfile(
                name = nameToSend, // Username value goes in 'name' field
                username = null,  // Not used
                interests = interestsList,
                profileImage = avatarToSend
                // Note: age, bio, gender, language NOT sent - set during registration only
            )

            result.onSuccess { user ->
                Log.d(TAG, "Profile updated successfully!")
                Log.d(TAG, "Updated user data:")
                Log.d(TAG, "  - Name: ${user.name}")
                Log.d(TAG, "  - Username: ${user.username}")
                Log.d(TAG, "  - Age: ${user.age}")
                Log.d(TAG, "  - Bio: ${user.bio}")
                Log.d(TAG, "  - Profile Image: ${user.profileImage}")
                
                // Log API response with specific tag
                android.util.Log.d("editprofilelog", "=== API Response - Save Profile ===")
                android.util.Log.d("editprofilelog", "Response Status: SUCCESS")
                android.util.Log.d("editprofilelog", "User ID: ${user.id}")
                android.util.Log.d("editprofilelog", "Name: ${user.name}")
                android.util.Log.d("editprofilelog", "Username: ${user.username}")
                android.util.Log.d("editprofilelog", "Phone: ${user.phone}")
                android.util.Log.d("editprofilelog", "Age: ${user.age}")
                android.util.Log.d("editprofilelog", "Bio: ${user.bio}")
                android.util.Log.d("editprofilelog", "Gender: ${user.gender}")
                android.util.Log.d("editprofilelog", "Language: ${user.language}")
                android.util.Log.d("editprofilelog", "Profile Image: ${user.profileImage}")
                android.util.Log.d("editprofilelog", "Interests: ${user.interests}")
                android.util.Log.d("editprofilelog", "Coin Balance: ${user.coinBalance}")
                android.util.Log.d("editprofilelog", "Is Verified: ${user.isVerified}")
                android.util.Log.d("editprofilelog", "Audio Call Enabled: ${user.audioCallEnabled}")
                android.util.Log.d("editprofilelog", "Video Call Enabled: ${user.videoCallEnabled}")

                android.util.Log.d("editprofilelog", "Rating: ${user.rating}")
                android.util.Log.d("editprofilelog", "Total Ratings: ${user.totalRatings}")
                android.util.Log.d("editprofilelog", "Is Online: ${user.isOnline}")
                android.util.Log.d("editprofilelog", "Last Seen: ${user.lastSeen}")

                android.util.Log.d("editprofilelog", "Total Earnings: ${user.totalEarnings}")
                android.util.Log.d("editprofilelog", "=== End API Response ===")
                
                // Update session with new data
                sessionManager.updateUserProfile(
                    name = user.name,
                    username = user.username,
                    age = user.age,
                    bio = user.bio,
                    profileImage = user.profileImage
                )
                
                Log.d(TAG, "Session updated with new user data")

                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to update profile", error)
                Log.e(TAG, "Error message: ${error.message}")
                
                // Log API error response with specific tag
                android.util.Log.e("editprofilelog", "=== API Response - Save Profile ===")
                android.util.Log.e("editprofilelog", "Response Status: FAILURE")
                android.util.Log.e("editprofilelog", "Error: ${error.message}")
                android.util.Log.e("editprofilelog", "Error Type: ${error.javaClass.simpleName}")
                error.printStackTrace()
                android.util.Log.e("editprofilelog", "=== End API Response ===")
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to update profile. Please try again."
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetSuccess() {
        _state.update { it.copy(isSuccess = false) }
    }
}

