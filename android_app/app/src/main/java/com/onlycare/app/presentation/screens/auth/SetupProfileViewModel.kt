package com.onlycare.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.remote.dto.AvatarDto
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupProfileState(
    val avatars: List<AvatarDto> = emptyList(),
    val isLoadingAvatars: Boolean = false,
    val isRegistering: Boolean = false,
    val avatarError: String? = null
)

@HiltViewModel
class SetupProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: ApiDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SetupProfileState())
    val state: StateFlow<SetupProfileState> = _state.asStateFlow()

    init {
        // Load avatars for the user's gender
        val gender = sessionManager.getGender()
        if (gender != null) {
            loadAvatars(gender)
        }
    }

    private val randomNames = listOf(
        "Aarohi", "Aditi", "Aishwarya", "Akshara", "Amrita", "Ananya", "Anika", "Anushka", "Avni", "Bhavya",
        "Chhavi", "Deepika", "Diya", "Esha", "Gauri", "Gia", "Hania", "Inaya", "Ishani", "Iva",
        "Janhvi", "Jiya", "Kaira", "Kavya", "Khushi", "Kiara", "Kriti", "Kyra", "Lakshmi", "Lipi",
        "Maheshwari", "Mahika", "Maira", "Meera", "Mishka", "Myra", "Navya", "Naysa", "Nia", "Nisha",
        "Ojaswi", "Pari", "Pihu", "Prisha", "Raveena", "Riya", "Ruhani", "Saanvi", "Sara", "Sejal",
        "Shanaya", "Siya", "Suhana", "Tanvi", "Tara", "Trisha", "Urvi", "Vanya", "Veda", "Zoya"
    )

    fun generateRandomName(): String {
        val name = randomNames.random()
        val suffix = (1000..9999).random()
        return "$name $suffix"
    }

    fun loadAvatars(gender: Gender) {
        _state.update { it.copy(isLoadingAvatars = true, avatarError = null) }
        
        viewModelScope.launch {
            val result = repository.getAvatars(gender)
            
            result.onSuccess { avatars ->
                _state.update {
                    it.copy(
                        avatars = avatars,
                        isLoadingAvatars = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoadingAvatars = false,
                        avatarError = "Failed to load avatars: ${error.message}"
                    )
                }
            }
        }
    }

    fun saveProfile(
        name: String, 
        age: Int, 
        bio: String, 
        profileImage: String = "",
        interests: List<String> = emptyList(),
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val gender = sessionManager.getGender()
        
        // Only register female users here
        if (gender != Gender.FEMALE) {
            // Male users should have been registered in SelectLanguageScreen
            // Just update session and proceed
            sessionManager.updateUserProfile(
                name = name,
                age = age,
                bio = bio,
                profileImage = profileImage
            )
            onSuccess()
            return
        }
        
        // Update profile information in session
        sessionManager.updateUserProfile(
            name = name,
            age = age,
            bio = bio,
            profileImage = profileImage
        )
        
        // Register female user
        registerFemaleUser(interests, onSuccess, onFailure)
    }
    
    private fun registerFemaleUser(
        interests: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val phone = sessionManager.getPhone()
        val gender = sessionManager.getGender()
        val avatar = sessionManager.getProfileImage()
        val language = sessionManager.getLanguage()
        val name = sessionManager.getName()
        val age = sessionManager.getAge()
        val bio = sessionManager.getBio()
        
        if (gender != Gender.FEMALE) {
            onFailure("Invalid gender for female registration")
            return
        }

        // =================================================================================
        // ✅ DEBUG MODE SUPPORT:
        // If user logged in via dummy OTP ("000000"), VerifyOTPViewModel stores a debug token.
        // Backend will reject /auth/register with "Please verify OTP first" because OTP wasn't
        // actually verified on server. In this case, bypass registration API and continue.
        // =================================================================================
        val authToken = sessionManager.getAuthToken().orEmpty()
        if (authToken.startsWith("debug_token_")) {
            android.util.Log.w("SetupProfileViewModel", "DEBUG OTP session detected - bypassing register API")
            sessionManager.saveUserSession(
                userId = "debug_${System.currentTimeMillis()}",
                phone = phone,
                gender = Gender.FEMALE,
                name = name,
                language = language ?: Language.ENGLISH,
                profileImage = avatar,
                bio = bio,
                age = age,
                coinBalance = 0
            )
            _state.update { it.copy(isRegistering = false) }
            onSuccess()
            return
        }
        
        _state.update { it.copy(isRegistering = true) }
        
        viewModelScope.launch {
            // Get referral code from session manager if available
            val referralCode = sessionManager.getReferralCode()
            
            repository.register(
                phone = phone,
                gender = gender ?: Gender.FEMALE,
                avatar = avatar,
                language = language ?: com.onlycare.app.domain.model.Language.ENGLISH,
                age = age,
                interests = interests.ifEmpty { null },
                description = bio,
                referralCode = referralCode
            ).onSuccess { response ->
                // Registration successful
                // Update SessionManager with user data from registration response
                // This includes the real user ID and profile_image URL that backend generated from avatar ID
                response.user?.let { user ->
                    val currentPhone = sessionManager.getPhone()
                    val currentGender = sessionManager.getGender() ?: Gender.FEMALE
                    val currentLanguage = sessionManager.getLanguage() ?: Language.ENGLISH
                    
                    // Save complete session with REAL user ID (replaces temp ID)
                    sessionManager.saveUserSession(
                        userId = user.id, // Real user ID from backend
                        phone = user.phone ?: currentPhone,
                        gender = if (user.gender == "FEMALE") Gender.FEMALE else Gender.MALE,
                        name = user.name,
                        username = user.username ?: "",
                        language = currentLanguage,
                        profileImage = user.profileImage ?: "", // Backend returns URL here
                        bio = user.bio ?: "",
                        age = user.age ?: 0,
                        coinBalance = user.coinBalance ?: 0
                    )
                }
                android.util.Log.d("SetupProfileViewModel", "Registration successful")
                _state.update { it.copy(isRegistering = false) }
                onSuccess()
            }.onFailure { error ->
                android.util.Log.e("SetupProfileViewModel", "Registration failed", error)
                _state.update { it.copy(isRegistering = false) }
                onFailure(error.message ?: "Registration failed")
            }
        }
    }
    
    fun getGender() = sessionManager.getGender()
    
    fun getSavedAvatar(): String = sessionManager.getProfileImage()
}


